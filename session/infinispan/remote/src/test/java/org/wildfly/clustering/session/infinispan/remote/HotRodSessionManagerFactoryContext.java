/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote;

import java.util.OptionalInt;

import org.infinispan.client.hotrod.DataFormat;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.RemoteCacheConfigurationBuilder;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.marshall.Marshaller;
import org.wildfly.clustering.cache.infinispan.marshalling.MediaTypes;
import org.wildfly.clustering.cache.infinispan.marshalling.UserMarshaller;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.context.AbstractContext;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.protostream.ClassLoaderMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamByteBufferMarshaller;
import org.wildfly.clustering.marshalling.protostream.SerializationContextBuilder;
import org.wildfly.clustering.server.eviction.EvictionConfiguration;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.session.SessionAttributePersistenceStrategy;
import org.wildfly.clustering.session.SessionManagerFactory;
import org.wildfly.clustering.session.SessionManagerFactoryConfiguration;

/**
 * A context for a HotRod session manager factory.
 * @param <CC> the container context type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public class HotRodSessionManagerFactoryContext<CC, SC> extends AbstractContext<SessionManagerFactory<CC, SC>> {
	private static final String SERVER_NAME = "server";

	private final SessionManagerFactory<CC, SC> factory;

	public HotRodSessionManagerFactoryContext(HotRodSessionManagerParameters parameters, String memberName, Supplier<SC> contextFactory) {
		RemoteCacheManager container = new RemoteCacheManager(parameters.getRemoteCacheContainerConfigurator().configure(new ConfigurationBuilder()));
		this.accept(container::close);

		ClassLoader loader = HotRodSessionManagerFactory.class.getClassLoader();
		Marshaller marshaller = new UserMarshaller(MediaTypes.WILDFLY_PROTOSTREAM, new ProtoStreamByteBufferMarshaller(SerializationContextBuilder.newInstance(ClassLoaderMarshaller.of(loader)).load(loader).build()));

		Configuration configuration = container.getConfiguration();
		OptionalInt sizeThreshold = parameters.getNearCacheMode().enabled() ? OptionalInt.of(Short.MAX_VALUE) : OptionalInt.empty();
		// Use local cache since our remote cluster has a single member
		// Reduce expiration interval to speed up expiration verification
		Consumer<RemoteCacheConfigurationBuilder> configurator = builder -> builder.configuration(
"""
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
}
""")
				.forceReturnValues(false)
				.marshaller(marshaller)
				.nearCacheMode(parameters.getNearCacheMode())
				.nearCacheFactory(parameters.getNearCacheMode().invalidated() ? new SessionManagerNearCacheFactory(new EvictionConfiguration() {
					@Override
					public OptionalInt getSizeThreshold() {
						return sizeThreshold;
					}
				}) : null)
				.transactionManagerLookup(org.infinispan.client.hotrod.transaction.lookup.RemoteTransactionManagerLookup.getInstance())
				.transactionMode(parameters.getTransactionMode())
				;
		String deploymentName = parameters.getDeploymentName();
		configuration.addRemoteCache(deploymentName, configurator);
		this.accept(() -> configuration.removeRemoteCache(deploymentName));
		SessionManagerFactoryConfiguration<SC> managerFactoryConfiguration = new SessionManagerFactoryConfiguration<>() {
			@Override
			public OptionalInt getSizeThreshold() {
				return sizeThreshold;
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
		// Entries are opaque to server
		DataFormat format = DataFormat.builder()
				.keyMarshaller(marshaller).keyType(MediaType.APPLICATION_OCTET_STREAM)
				.valueMarshaller(marshaller).valueType(MediaType.APPLICATION_OCTET_STREAM)
				.build();
		RemoteCache<?, ?> cache = container.getCache(parameters.getDeploymentName());
		cache.start();
		this.accept(cache::stop);

		this.factory = new HotRodSessionManagerFactory<>(new HotRodSessionManagerFactory.Configuration<SC>() {
			@Override
			public SessionManagerFactoryConfiguration<SC> getSessionManagerFactoryConfiguration() {
				return managerFactoryConfiguration;
			}

			@Override
			public RemoteCacheConfiguration getCacheConfiguration() {
				return RemoteCacheConfiguration.of(cache.withDataFormat(format));
			}
		});
		this.accept(this.factory::close);
	}

	@Override
	public SessionManagerFactory<CC, SC> get() {
		return this.factory;
	}
}
