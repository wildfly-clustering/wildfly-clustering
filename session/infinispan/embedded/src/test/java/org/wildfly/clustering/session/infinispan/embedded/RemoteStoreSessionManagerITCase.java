/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded;

import java.util.EnumSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.configuration.cache.CacheType;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.transaction.TransactionMode;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.wildfly.clustering.cache.infinispan.persistence.remote.RemoteCacheStoreConfigurationBuilder;
import org.wildfly.clustering.cache.infinispan.remote.InfinispanServerExtension;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.MarshallingTesterFactory;
import org.wildfly.clustering.session.SessionAttributePersistenceStrategy;
import org.wildfly.clustering.session.cache.AbstractSessionManagerITCase;

/**
 * Session manager integration test using an embedded Infinispan cache with a remote Infinispan store tested under a combination of settings.
 * @author Paul Ferraro
 */
public class RemoteStoreSessionManagerITCase extends AbstractSessionManagerITCase<InfinispanSessionManagerParameters> {

	@RegisterExtension
	static final InfinispanServerExtension INFINISPAN = new InfinispanServerExtension();

	static class InfinispanInvalidationSessionManagerArgumentsProvider implements ArgumentsProvider {
		Class<? extends MarshallingTesterFactory> marshallerClass = MarshallingTesterFactory.class;
		Set<TransactionMode> transactionModes = EnumSet.of(TransactionMode.NON_TRANSACTIONAL);
		Set<SessionAttributePersistenceStrategy> strategies = EnumSet.allOf(SessionAttributePersistenceStrategy.class);

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			Stream.Builder<Arguments> builder = Stream.builder();
			for (MarshallingTesterFactory factory : ServiceLoader.load(this.marshallerClass, this.marshallerClass.getClassLoader())) {
				ByteBufferMarshaller marshaller = factory.getMarshaller();
				for (SessionAttributePersistenceStrategy strategy : this.strategies) {
					for (TransactionMode mode : this.transactionModes) {
						builder.add(Arguments.of(new InfinispanSessionManagerParameters() {
							@Override
							public ByteBufferMarshaller getSessionAttributeMarshaller() {
								return marshaller;
							}

							@Override
							public SessionAttributePersistenceStrategy getSessionAttributePersistenceStrategy() {
								return strategy;
							}

							@Override
							public CacheType getCacheType() {
								return CacheType.INVALIDATION;
							}

							@Override
							public TransactionMode getTransactionMode() {
								return mode;
							}

							@Override
							public Runnable persistence(GlobalConfiguration global, PersistenceConfigurationBuilder builder) {
								org.infinispan.client.hotrod.configuration.Configuration configuration = INFINISPAN.configure(new org.infinispan.client.hotrod.configuration.ConfigurationBuilder().marshaller(global.serialization().marshaller()));
								RemoteCacheManager container = new RemoteCacheManager(configuration);

								builder.persistence().addStore(RemoteCacheStoreConfigurationBuilder.class)
										.container(container)
										.configuration(
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
			"interval" : 0
		},
		"transaction" : {
			"mode" : "NON_XA",
			"locking" : "PESSIMISTIC"
		}
	}
}
""")
										.segmented(true)
										.shared(true)
										// transactional(true) Currently fails with: java.lang.UnsupportedOperationException: Decorated caches should not delegate wrapping operations
										// See https://github.com/infinispan/infinispan/issues/14926
										.transactional(false)
										;
								return container::close;
							}

							@Override
							public String toString() {
								return Map.ofEntries(
										Map.entry(ByteBufferMarshaller.class.getSimpleName(), marshaller.toString()),
										Map.entry(SessionAttributePersistenceStrategy.class.getSimpleName(), strategy.name()),
										Map.entry(TransactionMode.class.getSimpleName(), mode.name())
									).toString();
							}
						}));
					}
				}
			}
			return builder.build();
		}
	}

	RemoteStoreSessionManagerITCase() {
		super(InfinispanSessionManagerFactoryContext::new);
	}

	@Override
	@ParameterizedTest
	@ArgumentsSource(InfinispanInvalidationSessionManagerArgumentsProvider.class)
	public void basic(InfinispanSessionManagerParameters parameters) {
		super.basic(parameters);
	}

	@Override
	@ParameterizedTest
	@ArgumentsSource(InfinispanInvalidationSessionManagerArgumentsProvider.class)
	public void concurrent(InfinispanSessionManagerParameters parameters) throws InterruptedException, ExecutionException {
		super.concurrent(parameters);
	}

	@Override
	@ParameterizedTest
	@ArgumentsSource(InfinispanInvalidationSessionManagerArgumentsProvider.class)
	public void expiration(InfinispanSessionManagerParameters parameters) throws InterruptedException {
		super.expiration(parameters);
	}
}
