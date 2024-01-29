/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.wildfly.clustering.cache.infinispan.batch.TransactionBatch;
import org.wildfly.clustering.cache.infinispan.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.protostream.ClassLoaderMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.PassivationListener;
import org.wildfly.clustering.session.SessionAttributePersistenceStrategy;
import org.wildfly.clustering.session.SessionManagerFactory;
import org.wildfly.clustering.session.SessionManagerFactoryProvider;
import org.wildfly.clustering.session.container.ContainerFacadeProvider;

/**
 * @author Paul Ferraro
 */
public class HotRodSessionManagerFactoryProvider<DC> implements SessionManagerFactoryProvider<DC, TransactionBatch> {

	private static final String SERVER_NAME = "server";
	private static final String DEPLOYMENT_NAME_PATTERN = "%s-%s.war";

	private final HotRodSessionManagerParameters parameters;
	private final RemoteCacheContainer container;
	private final String deploymentName;

	public HotRodSessionManagerFactoryProvider(HotRodSessionManagerParameters parameters, String memberName) throws Exception {
		this.parameters = parameters;
		this.deploymentName = String.format(DEPLOYMENT_NAME_PATTERN, parameters.getSessionAttributePersistenceStrategy().name(), parameters.getNearCacheMode().name());

		ClassLoader loader = HotRodSessionManagerFactory.class.getClassLoader();
		this.container = parameters.createRemoteCacheContainer(new ConfigurationBuilder().marshaller(new ProtoStreamMarshaller(ClassLoaderMarshaller.of(loader), builder -> builder.require(loader))).classLoader(loader));
		this.container.start();

		container.getConfiguration().addRemoteCache(this.deploymentName, builder -> builder.forceReturnValues(false).nearCacheMode(parameters.getNearCacheMode()).transactionMode(TransactionMode.NONE).templateName(DefaultTemplate.LOCAL));
	}

	@Override
	public <SC> SessionManagerFactory<DC, SC, TransactionBatch> createSessionManagerFactory(Supplier<SC> contextFactory, ContainerFacadeProvider<Entry<ImmutableSession, DC>, DC, PassivationListener<DC>> provider) {
		HotRodSessionManagerFactoryConfiguration<Map.Entry<ImmutableSession, DC>, DC, PassivationListener<DC>, SC> managerFactoryConfiguration = new HotRodSessionManagerFactoryConfiguration<>() {
			@Override
			public Integer getMaxActiveSessions() {
				return HotRodSessionManagerFactoryProvider.this.parameters.getNearCacheMode().enabled() ? Integer.valueOf(Short.MAX_VALUE) : null;
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
			public ContainerFacadeProvider<Map.Entry<ImmutableSession, DC>, DC, PassivationListener<DC>> getContainerFacadeProvider() {
				return provider;
			}

			@Override
			public SessionAttributePersistenceStrategy getAttributePersistenceStrategy() {
				return HotRodSessionManagerFactoryProvider.this.parameters.getSessionAttributePersistenceStrategy();
			}

			@Override
			public String getDeploymentName() {
				return HotRodSessionManagerFactoryProvider.this.deploymentName;
			}

			@Override
			public String getServerName() {
				return SERVER_NAME;
			}

			@Override
			public <K, V> RemoteCache<K, V> getCache() {
				return HotRodSessionManagerFactoryProvider.this.container.getCache(this.getDeploymentName());
			}

			@Override
			public int getExpirationThreadPoolSize() {
				return 16;
			}
		};
		return new HotRodSessionManagerFactory<>(managerFactoryConfiguration);
	}

	@Override
	public void close() throws Exception {
		try {
			this.container.getConfiguration().removeRemoteCache(this.deploymentName);
		} finally {
			this.container.stop();
		}
	}
}
