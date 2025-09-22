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
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheManagerContext;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheProperties;
import org.wildfly.clustering.context.AbstractContext;
import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.server.group.GroupCommandDispatcherFactory;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.server.infinispan.dispatcher.CacheContainerCommandDispatcherFactory;
import org.wildfly.clustering.server.infinispan.dispatcher.ChannelEmbeddedCacheManagerCommandDispatcherFactoryConfiguration;
import org.wildfly.clustering.server.infinispan.dispatcher.EmbeddedCacheManagerCommandDispatcherFactory;
import org.wildfly.clustering.server.jgroups.ChannelGroupMember;
import org.wildfly.clustering.server.jgroups.dispatcher.ChannelCommandDispatcherFactory;
import org.wildfly.clustering.server.jgroups.dispatcher.ChannelCommandDispatcherFactoryContext;
import org.wildfly.clustering.session.SessionAttributePersistenceStrategy;
import org.wildfly.clustering.session.SessionManagerFactory;
import org.wildfly.clustering.session.SessionManagerFactoryConfiguration;
import org.wildfly.clustering.session.cache.MockSessionSpecificationProvider;

/**
 * @param <C> the session manager context type
 * @param <SC> the referenced session context type
 * @author Paul Ferraro
 */
public class InfinispanSessionManagerFactoryContext<C, SC> extends AbstractContext<SessionManagerFactory<C, SC>> {
	private static final String SERVER_NAME = "server";

	private final SessionManagerFactory<C, SC> factory;

	public InfinispanSessionManagerFactoryContext(InfinispanSessionManagerParameters parameters, String memberName, Supplier<SC> contextFactory) {
		try {
			Context<ChannelCommandDispatcherFactory> dispatcherFactoryContext = new ChannelCommandDispatcherFactoryContext(parameters.getClusterName(), memberName);
			this.accept(dispatcherFactoryContext::close);
			Context<EmbeddedCacheManager> managerContext = new EmbeddedCacheManagerContext(dispatcherFactoryContext.get().getGroup().getChannel());
			this.accept(managerContext::close);
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.clustering().cacheType(parameters.getCacheType());
			builder.clustering().hash().numSegments(16);
			TransactionMode transactionMode = parameters.getTransactionMode();
			builder.transaction().transactionMode(transactionMode);
			if (transactionMode.isTransactional()) {
				builder.transaction().lockingMode(LockingMode.PESSIMISTIC);
				builder.transaction().transactionManagerLookup(EmbeddedTransactionManager::getInstance);
				builder.locking().isolationLevel(IsolationLevel.REPEATABLE_READ);
			}
			String deploymentName = parameters.getDeploymentName();

			if (parameters.getCacheType() == CacheType.INVALIDATION) {
				parameters.persistence(managerContext.get().getCacheManagerConfiguration(), builder.persistence());
			}

			Configuration configuration = builder.build();
			CacheProperties properties = new EmbeddedCacheProperties(configuration);
			Assertions.assertThat(properties.isLockOnRead()).isEqualTo(transactionMode.isTransactional());
			Assertions.assertThat(properties.isLockOnWrite()).isEqualTo(transactionMode.isTransactional());
			Assertions.assertThat(properties.isMarshalling()).isTrue();
			Assertions.assertThat(properties.isPersistent()).isTrue();
			Assertions.assertThat(properties.isTransactional()).isEqualTo(transactionMode.isTransactional());
			managerContext.get().defineConfiguration(deploymentName, configuration);
			this.accept(() -> managerContext.get().undefineConfiguration(deploymentName));

			CacheContainerCommandDispatcherFactory commandDispatcherFactory = new EmbeddedCacheManagerCommandDispatcherFactory<>(new ChannelEmbeddedCacheManagerCommandDispatcherFactoryConfiguration() {
				@Override
				public EmbeddedCacheManager getCacheContainer() {
					return managerContext.get();
				}

				@Override
				public GroupCommandDispatcherFactory<org.jgroups.Address, ChannelGroupMember> getCommandDispatcherFactory() {
					return dispatcherFactoryContext.get();
				}
			});
			SessionManagerFactoryConfiguration<SC> managerFactoryConfiguration = new SessionManagerFactoryConfiguration<>() {
				@Override
				public OptionalInt getMaxSize() {
					return OptionalInt.of(1);
				}

				@Override
				public ByteBufferMarshaller getMarshaller() {
					return parameters.getSessionAttributeMarshaller();
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
					return parameters.getSessionAttributePersistenceStrategy();
				}

				@Override
				public String getDeploymentName() {
					return parameters.getDeploymentName();
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
			Cache<?, ?> cache = managerContext.get().getCache(parameters.getDeploymentName());
			cache.start();
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
			this.factory = new InfinispanSessionManagerFactory<>(managerFactoryConfiguration, provider, provider, infinispan);
			this.accept(this.factory::close);
		} catch (RuntimeException | Error e) {
			this.close();
			throw e;
		}
	}

	@Override
	public SessionManagerFactory<C, SC> get() {
		return this.factory;
	}
}
