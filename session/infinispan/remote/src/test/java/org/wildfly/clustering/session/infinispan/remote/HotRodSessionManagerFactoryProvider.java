/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote;

import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.RemoteCacheConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.commons.marshall.Marshaller;
import org.wildfly.clustering.cache.infinispan.marshalling.MediaTypes;
import org.wildfly.clustering.cache.infinispan.marshalling.UserMarshaller;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.protostream.ClassLoaderMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamByteBufferMarshaller;
import org.wildfly.clustering.marshalling.protostream.SerializationContextBuilder;
import org.wildfly.clustering.server.AutoCloseableProvider;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.session.SessionAttributePersistenceStrategy;
import org.wildfly.clustering.session.SessionManagerFactory;
import org.wildfly.clustering.session.SessionManagerFactoryConfiguration;
import org.wildfly.clustering.session.cache.MockSessionSpecificationProvider;
import org.wildfly.clustering.session.cache.SessionManagerFactoryProvider;

/**
 * @param <C> the session manager context type
 * @author Paul Ferraro
 */
public class HotRodSessionManagerFactoryProvider<C> extends AutoCloseableProvider implements SessionManagerFactoryProvider<C> {
	private static final String SERVER_NAME = "server";
	private static final String DEPLOYMENT_NAME_PATTERN = "%s-%s.war";

	private final HotRodSessionManagerParameters parameters;
	private final RemoteCacheContainer container;
	private final String deploymentName;

	public HotRodSessionManagerFactoryProvider(HotRodSessionManagerParameters parameters, String memberName) {
		this.parameters = parameters;
		this.deploymentName = String.format(DEPLOYMENT_NAME_PATTERN, parameters.getSessionAttributeMarshaller(), parameters.getSessionAttributePersistenceStrategy().name());

		ClassLoader loader = HotRodSessionManagerFactory.class.getClassLoader();
		Marshaller marshaller = new UserMarshaller(MediaTypes.WILDFLY_PROTOSTREAM, new ProtoStreamByteBufferMarshaller(SerializationContextBuilder.newInstance(ClassLoaderMarshaller.of(loader)).load(loader).build()));
		this.container = new RemoteCacheManager(parameters.getRemoteCacheContainerConfigurator().configure(new ConfigurationBuilder().marshaller(marshaller)), false);
		this.container.start();
		this.accept(this.container::stop);

		Configuration configuration = this.container.getConfiguration();
		// Use local cache since our remote cluster has a single member
		// Reduce expiration interval to speed up expiration verification
		Consumer<RemoteCacheConfigurationBuilder> configurator = builder -> builder.configuration("""
{
	"local-cache" : {
		"expiration" : {
			"interval" : 1000
		},
		"transaction" : {
			"mode" : "NON_XA",
			"locking" : "PESSIMISTIC"
		}
	}
}""")
				.forceReturnValues(false)
				.nearCacheMode(parameters.getNearCacheMode())
				.transactionMode(TransactionMode.NONE)
// Currently fails due to https://github.com/infinispan/infinispan/issues/14926
//				.transactionMode(TransactionMode.NON_XA)
//				.transactionManagerLookup(org.infinispan.client.hotrod.transaction.lookup.RemoteTransactionManagerLookup.getInstance())
				;
		configuration.addRemoteCache(this.deploymentName, configurator);
		this.accept(() -> configuration.removeRemoteCache(this.deploymentName));
	}

	@Override
	public <SC> SessionManagerFactory<C, SC> createSessionManagerFactory(Supplier<SC> contextFactory) {
		SessionManagerFactoryConfiguration<SC> managerFactoryConfiguration = new SessionManagerFactoryConfiguration<>() {
			@Override
			public OptionalInt getMaxActiveSessions() {
				return HotRodSessionManagerFactoryProvider.this.parameters.getNearCacheMode().enabled() ? OptionalInt.of(Short.MAX_VALUE) : OptionalInt.empty();
			}

			@Override
			public ByteBufferMarshaller getMarshaller() {
				return HotRodSessionManagerFactoryProvider.this.parameters.getSessionAttributeMarshaller();
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

			@Override
			public ClassLoader getClassLoader() {
				return this.getClass().getClassLoader();
			}
		};
		RemoteCache<?, ?> cache = this.container.getCache(this.deploymentName);
		cache.start();
		this.accept(cache::stop);
		RemoteCacheConfiguration hotrod = new RemoteCacheConfiguration() {
			@SuppressWarnings("unchecked")
			@Override
			public <CK, CV> RemoteCache<CK, CV> getCache() {
				return (RemoteCache<CK, CV>) cache;
			}
		};
		MockSessionSpecificationProvider<C> provider = new MockSessionSpecificationProvider<>();
		return new HotRodSessionManagerFactory<>(managerFactoryConfiguration, provider, provider, hotrod);
	}
}
