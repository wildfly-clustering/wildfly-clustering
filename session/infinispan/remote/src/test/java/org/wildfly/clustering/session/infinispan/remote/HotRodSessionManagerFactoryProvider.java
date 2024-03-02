/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote;

import java.util.OptionalInt;
import java.util.function.Supplier;

import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.commons.marshall.Marshaller;
import org.wildfly.clustering.cache.infinispan.batch.TransactionBatch;
import org.wildfly.clustering.cache.infinispan.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.protostream.ClassLoaderMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.session.SessionAttributePersistenceStrategy;
import org.wildfly.clustering.session.SessionManagerFactory;
import org.wildfly.clustering.session.SessionManagerFactoryConfiguration;
import org.wildfly.clustering.session.cache.MockSessionSpecificationProvider;
import org.wildfly.clustering.session.cache.SessionManagerFactoryProvider;

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
		Marshaller marshaller = new ProtoStreamMarshaller(ClassLoaderMarshaller.of(loader), builder -> builder.require(loader));
		this.container = new RemoteCacheManager(parameters.getRemoteCacheContainerConfigurator().configure(new ConfigurationBuilder().marshaller(marshaller).classLoader(loader)), false);
		this.container.start();

		container.getConfiguration().addRemoteCache(this.deploymentName, builder -> builder.forceReturnValues(false).nearCacheMode(parameters.getNearCacheMode()).transactionMode(TransactionMode.NONE).templateName(DefaultTemplate.LOCAL));
	}

	@Override
	public <SC> SessionManagerFactory<DC, SC, TransactionBatch> createSessionManagerFactory(Supplier<SC> contextFactory) {
		SessionManagerFactoryConfiguration<SC> managerFactoryConfiguration = new SessionManagerFactoryConfiguration<>() {
			@Override
			public OptionalInt getMaxActiveSessions() {
				return HotRodSessionManagerFactoryProvider.this.parameters.getNearCacheMode().enabled() ? OptionalInt.of(Short.MAX_VALUE) : OptionalInt.empty();
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
		};
		RemoteCacheConfiguration hotrod = new RemoteCacheConfiguration() {
			@Override
			public <CK, CV> RemoteCache<CK, CV> getCache() {
				return HotRodSessionManagerFactoryProvider.this.container.getCache(HotRodSessionManagerFactoryProvider.this.deploymentName);
			}
		};
		return new HotRodSessionManagerFactory<>(managerFactoryConfiguration, new MockSessionSpecificationProvider<>(), hotrod);
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
