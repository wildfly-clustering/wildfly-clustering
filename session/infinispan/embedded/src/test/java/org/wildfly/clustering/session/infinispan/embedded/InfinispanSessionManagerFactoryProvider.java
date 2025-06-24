/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded;

import java.util.OptionalInt;
import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheType;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.IsolationLevel;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.transaction.tm.EmbeddedTransactionManager;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheProperties;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.server.AutoCloseableProvider;
import org.wildfly.clustering.server.group.GroupCommandDispatcherFactory;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.server.infinispan.EmbeddedCacheManagerFactory;
import org.wildfly.clustering.server.infinispan.dispatcher.CacheContainerCommandDispatcherFactory;
import org.wildfly.clustering.server.infinispan.dispatcher.ChannelEmbeddedCacheManagerCommandDispatcherFactoryConfiguration;
import org.wildfly.clustering.server.infinispan.dispatcher.EmbeddedCacheManagerCommandDispatcherFactory;
import org.wildfly.clustering.server.jgroups.ChannelGroupMember;
import org.wildfly.clustering.server.jgroups.ForkChannelFactory;
import org.wildfly.clustering.server.jgroups.dispatcher.ChannelCommandDispatcherFactoryProvider;
import org.wildfly.clustering.session.SessionAttributePersistenceStrategy;
import org.wildfly.clustering.session.SessionManagerFactory;
import org.wildfly.clustering.session.SessionManagerFactoryConfiguration;
import org.wildfly.clustering.session.cache.MockSessionSpecificationProvider;
import org.wildfly.clustering.session.cache.SessionManagerFactoryProvider;

/**
 * @param <C> the session manager context type
 * @author Paul Ferraro
 */
@org.infinispan.notifications.Listener
public class InfinispanSessionManagerFactoryProvider<C> extends AutoCloseableProvider implements SessionManagerFactoryProvider<C> {
	private static final System.Logger LOGGER = System.getLogger(InfinispanSessionManagerFactoryProvider.class.getName());
	private static final String CONTAINER_NAME = "container";
	private static final String SERVER_NAME = "server";

	private final InfinispanSessionManagerParameters parameters;
	private final ChannelCommandDispatcherFactoryProvider dispatcherFactoryProvider;
	private final EmbeddedCacheManager manager;

	public InfinispanSessionManagerFactoryProvider(InfinispanSessionManagerParameters parameters, String memberName) {
		this.parameters = parameters;
		this.dispatcherFactoryProvider = new ChannelCommandDispatcherFactoryProvider(parameters.getClusterName(), memberName);
		try {
			this.accept(this.dispatcherFactoryProvider::close);
			this.manager = new EmbeddedCacheManagerFactory(new ForkChannelFactory(this.dispatcherFactoryProvider.getChannel()), parameters.getClusterName(), memberName).apply(CONTAINER_NAME, InfinispanSessionManagerFactoryConfiguration.class.getClassLoader());
			this.manager.start();
			this.accept(this.manager::stop);
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.clustering().cacheType(parameters.getCacheType());
			TransactionMode transactionMode = parameters.getTransactionMode();
			builder.transaction().transactionMode(transactionMode);
			if (transactionMode.isTransactional()) {
				builder.transaction().lockingMode(LockingMode.PESSIMISTIC);
				builder.transaction().transactionManagerLookup(EmbeddedTransactionManager::getInstance);
				builder.locking().isolationLevel(IsolationLevel.REPEATABLE_READ);
			}
			String deploymentName = this.parameters.getDeploymentName();

			if (parameters.getCacheType() == CacheType.INVALIDATION) {
				parameters.persistence(this.manager.getCacheManagerConfiguration(), builder.persistence());
			}

			Configuration configuration = builder.build();
			CacheProperties properties = new EmbeddedCacheProperties(configuration);
			Assertions.assertThat(properties.isActive()).isTrue();
			Assertions.assertThat(properties.isLockOnRead()).isEqualTo(transactionMode.isTransactional());
			Assertions.assertThat(properties.isLockOnWrite()).isEqualTo(transactionMode.isTransactional());
			Assertions.assertThat(properties.isMarshalling()).isTrue();
			Assertions.assertThat(properties.isPersistent()).isTrue();
			Assertions.assertThat(properties.isTransactional()).isEqualTo(transactionMode.isTransactional());
			this.manager.defineConfiguration(deploymentName, configuration);
			this.accept(() -> this.manager.undefineConfiguration(deploymentName));
		} catch (RuntimeException | Error e) {
			this.close();
			throw e;
		}
	}

	@Override
	public <SC> SessionManagerFactory<C, SC> createSessionManagerFactory(Supplier<SC> contextFactory) {
		CacheContainerCommandDispatcherFactory commandDispatcherFactory = new EmbeddedCacheManagerCommandDispatcherFactory<>(new ChannelEmbeddedCacheManagerCommandDispatcherFactoryConfiguration() {
			@Override
			public EmbeddedCacheManager getCacheContainer() {
				return InfinispanSessionManagerFactoryProvider.this.manager;
			}

			@Override
			public GroupCommandDispatcherFactory<org.jgroups.Address, ChannelGroupMember> getCommandDispatcherFactory() {
				return InfinispanSessionManagerFactoryProvider.this.dispatcherFactoryProvider.getCommandDispatcherFactory();
			}
		});
		SessionManagerFactoryConfiguration<SC> managerFactoryConfiguration = new SessionManagerFactoryConfiguration<>() {
			@Override
			public OptionalInt getMaxActiveSessions() {
				return OptionalInt.of(1);
			}

			@Override
			public ByteBufferMarshaller getMarshaller() {
				return InfinispanSessionManagerFactoryProvider.this.parameters.getSessionAttributeMarshaller();
			}

			@Override
			public Supplier<SC> getSessionContextFactory() {
				return contextFactory;
			}

			@Override
			public Immutability getImmutability() {
				return Immutability.getDefault();
			}

			@Override
			public SessionAttributePersistenceStrategy getAttributePersistenceStrategy() {
				return InfinispanSessionManagerFactoryProvider.this.parameters.getSessionAttributePersistenceStrategy();
			}

			@Override
			public String getDeploymentName() {
				return InfinispanSessionManagerFactoryProvider.this.parameters.getDeploymentName();
			}

			@Override
			public String getServerName() {
				return SERVER_NAME;
			}

			@Override
			public ClassLoader getClassLoader() {
				return this.getClass().getClassLoader();
			}
		};
		Cache<?, ?> cache = this.manager.getCache(this.parameters.getDeploymentName());
		cache.start();
		if (cache.getCacheConfiguration().clustering().cacheMode().isInvalidation()) {
			cache.addListener(this);
			this.accept(() -> cache.removeListener(this));
		}
		this.accept(cache::stop);
		InfinispanSessionManagerFactoryConfiguration infinispan = new InfinispanSessionManagerFactoryConfiguration() {
			@SuppressWarnings("unchecked")
			@Override
			public <K, V> Cache<K, V> getCache() {
				return (Cache<K, V>) cache;
			}

			@Override
			public CacheContainerCommandDispatcherFactory getCommandDispatcherFactory() {
				return commandDispatcherFactory;
			}
		};
		MockSessionSpecificationProvider<C> provider = new MockSessionSpecificationProvider<>();
		return new InfinispanSessionManagerFactory<>(managerFactoryConfiguration, provider, provider, infinispan);
	}

	@org.infinispan.notifications.cachelistener.annotation.CacheEntryInvalidated
	public void invalidated(org.infinispan.notifications.cachelistener.event.CacheEntryInvalidatedEvent<?, ?> event) {
		LOGGER.log(System.Logger.Level.DEBUG, "{1}-invalidate {0}", event.getKey(), event.isPre() ? "Pre" : "Post");
	}
}
