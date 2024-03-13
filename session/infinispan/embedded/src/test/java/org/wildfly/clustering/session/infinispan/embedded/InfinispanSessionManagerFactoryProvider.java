/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded;

import java.util.OptionalInt;
import java.util.function.Supplier;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.tm.EmbeddedTransactionManager;
import org.infinispan.util.concurrent.IsolationLevel;
import org.wildfly.clustering.cache.infinispan.batch.TransactionBatch;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.server.group.GroupCommandDispatcherFactory;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.infinispan.EmbeddedCacheManagerFactory;
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
public class InfinispanSessionManagerFactoryProvider<C> implements SessionManagerFactoryProvider<C, TransactionBatch> {
	private static final String CONTAINER_NAME = "container";
	private static final String SERVER_NAME = "server";
	private static final String DEPLOYMENT_NAME = "test.war";

	private final InfinispanSessionManagerParameters parameters;
	private final ChannelCommandDispatcherFactoryProvider dispatcherFactoryProvider;
	private final EmbeddedCacheManager manager;

	public InfinispanSessionManagerFactoryProvider(InfinispanSessionManagerParameters parameters, String memberName) throws Exception {
		this.parameters = parameters;
		this.dispatcherFactoryProvider = new ChannelCommandDispatcherFactoryProvider(parameters.getClusterName(), memberName);
		this.manager = new EmbeddedCacheManagerFactory(new ForkChannelFactory(this.dispatcherFactoryProvider.getChannel()), parameters.getClusterName(), memberName).apply(CONTAINER_NAME, InfinispanSessionManagerFactoryConfiguration.class.getClassLoader());
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.clustering().cacheMode(parameters.getCacheMode());
		builder.transaction().lockingMode(LockingMode.PESSIMISTIC);
		builder.transaction().transactionMode(parameters.getTransactionMode());
		if (parameters.getTransactionMode().isTransactional()) {
			builder.transaction().transactionManagerLookup(() -> EmbeddedTransactionManager.getInstance());
			builder.locking().isolationLevel(IsolationLevel.REPEATABLE_READ);
		}
		this.manager.defineConfiguration(DEPLOYMENT_NAME, builder.build());
	}

	@Override
	public <SC> SessionManagerFactory<C, SC, TransactionBatch> createSessionManagerFactory(Supplier<SC> contextFactory) {
		GroupCommandDispatcherFactory<Address, CacheContainerGroupMember> commandDispatcherFactory = new EmbeddedCacheManagerCommandDispatcherFactory<>(new ChannelEmbeddedCacheManagerCommandDispatcherFactoryConfiguration() {
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
				return ProtoStreamTesterFactory.INSTANCE.getMarshaller();
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
				return DEPLOYMENT_NAME;
			}

			@Override
			public String getServerName() {
				return SERVER_NAME;
			}
		};
		InfinispanSessionManagerFactoryConfiguration<CacheContainerGroupMember> infinispan = new InfinispanSessionManagerFactoryConfiguration<>() {
			@Override
			public <K, V> Cache<K, V> getCache() {
				return InfinispanSessionManagerFactoryProvider.this.manager.getCache(DEPLOYMENT_NAME);
			}

			@Override
			public GroupCommandDispatcherFactory<Address, CacheContainerGroupMember> getCommandDispatcherFactory() {
				return commandDispatcherFactory;
			}
		};
		MockSessionSpecificationProvider<C> provider = new MockSessionSpecificationProvider<>();
		return new InfinispanSessionManagerFactory<>(managerFactoryConfiguration, provider, provider, infinispan);
	}

	@Override
	public void close() throws Exception {
		this.manager.getCache(DEPLOYMENT_NAME).clear();
		this.manager.close();
		this.dispatcherFactoryProvider.close();
	}
}
