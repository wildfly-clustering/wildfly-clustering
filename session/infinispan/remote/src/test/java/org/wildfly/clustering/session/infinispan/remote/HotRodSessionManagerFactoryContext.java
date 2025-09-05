/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote;

import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.infinispan.client.hotrod.DataFormat;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.RemoteCacheConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.marshall.Marshaller;
import org.wildfly.clustering.cache.infinispan.marshalling.MediaTypes;
import org.wildfly.clustering.cache.infinispan.marshalling.UserMarshaller;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.context.AbstractContext;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.protostream.ClassLoaderMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamByteBufferMarshaller;
import org.wildfly.clustering.marshalling.protostream.SerializationContextBuilder;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.session.SessionAttributePersistenceStrategy;
import org.wildfly.clustering.session.SessionManagerFactory;
import org.wildfly.clustering.session.SessionManagerFactoryConfiguration;
import org.wildfly.clustering.session.cache.MockSessionSpecificationProvider;

/**
 * @param <C> the session manager context type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public class HotRodSessionManagerFactoryContext<C, SC> extends AbstractContext<SessionManagerFactory<C, SC>> {
	private static final String SERVER_NAME = "server";

	private final SessionManagerFactory<C, SC> factory;

	public HotRodSessionManagerFactoryContext(HotRodSessionManagerParameters parameters, String memberName, Supplier<SC> contextFactory) {
		ClassLoader loader = HotRodSessionManagerFactory.class.getClassLoader();
		Marshaller marshaller = new UserMarshaller(MediaTypes.WILDFLY_PROTOSTREAM, new ProtoStreamByteBufferMarshaller(SerializationContextBuilder.newInstance(ClassLoaderMarshaller.of(loader)).load(loader).build()));
		RemoteCacheManager container = new RemoteCacheManager(parameters.getRemoteCacheContainerConfigurator().configure(new ConfigurationBuilder().marshaller(marshaller)));
		this.accept(container::close);

		Configuration configuration = container.getConfiguration();
		// Use local cache since our remote cluster has a single member
		// Reduce expiration interval to speed up expiration verification
		Consumer<RemoteCacheConfigurationBuilder> configurator = builder -> builder.configuration("""
{
	"local-cache" : {
		"encoding" : {
			"key" : {
				"media-type" : "application/octet-stream"
			},
			"value" : {
				"media-type" : "application/octet-stream"
			}
		},
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
// Currently fails with: java.lang.UnsupportedOperationException: Decorated caches should not delegate wrapping operations
// See https://github.com/infinispan/infinispan/issues/14926
//				.transactionMode(TransactionMode.NON_XA)
				.transactionMode(TransactionMode.NONE)
				.transactionManagerLookup(org.infinispan.client.hotrod.transaction.lookup.RemoteTransactionManagerLookup.getInstance())
				;
		String deploymentName = parameters.getDeploymentName();
		configuration.addRemoteCache(deploymentName, configurator);
		this.accept(() -> configuration.removeRemoteCache(deploymentName));
		SessionManagerFactoryConfiguration<SC> managerFactoryConfiguration = new SessionManagerFactoryConfiguration<>() {
			@Override
			public OptionalInt getMaxActiveSessions() {
				return parameters.getNearCacheMode().enabled() ? OptionalInt.of(Short.MAX_VALUE) : OptionalInt.empty();
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
		DataFormat format = DataFormat.builder()
				.keyType(MediaType.APPLICATION_OBJECT).keyMarshaller(marshaller)
				.valueType(MediaType.APPLICATION_OBJECT).valueMarshaller(marshaller)
				.build();
		RemoteCache<?, ?> cache = container.getCache(parameters.getDeploymentName());
		cache.start();
		this.accept(cache::stop);
		RemoteCacheConfiguration hotrod = new RemoteCacheConfiguration() {
			@SuppressWarnings("unchecked")
			@Override
			public <CK, CV> RemoteCache<CK, CV> getCache() {
				return (RemoteCache<CK, CV>) cache.withDataFormat(format);
			}
		};
		MockSessionSpecificationProvider<C> provider = new MockSessionSpecificationProvider<>();
		this.factory = new HotRodSessionManagerFactory<>(managerFactoryConfiguration, provider, provider, hotrod);
		this.accept(this.factory::close);
	}

	@Override
	public SessionManagerFactory<C, SC> get() {
		return this.factory;
	}
}
