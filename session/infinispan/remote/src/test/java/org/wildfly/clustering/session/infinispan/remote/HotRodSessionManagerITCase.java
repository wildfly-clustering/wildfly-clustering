/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote;

import java.util.EnumSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.wildfly.clustering.cache.infinispan.remote.InfinispanServerExtension;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheContainerConfigurator;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.MarshallingTesterFactory;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.session.SessionAttributePersistenceStrategy;
import org.wildfly.clustering.session.cache.SessionManagerITCase;

/**
 * Session manager integration test using an Infinispan server container.
 * @author Paul Ferraro
 */
public class HotRodSessionManagerITCase extends SessionManagerITCase<HotRodSessionManagerParameters> {

	@RegisterExtension
	static final InfinispanServerExtension INFINISPAN = new InfinispanServerExtension();

	static class HotRodSessionManagerArgumentsProvider implements ArgumentsProvider {
		Class<? extends MarshallingTesterFactory> marshallerClass = MarshallingTesterFactory.class;
		Set<NearCacheMode> nearCacheModes = EnumSet.of(NearCacheMode.DISABLED);
		// Currently fails with: java.lang.UnsupportedOperationException: Decorated caches should not delegate wrapping operations
		// See https://github.com/infinispan/infinispan/issues/14926
		Set<TransactionMode> transactionModes = EnumSet.of(TransactionMode.NONE);

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			Stream.Builder<Arguments> builder = Stream.builder();
			for (MarshallingTesterFactory factory : ServiceLoader.load(this.marshallerClass, this.marshallerClass.getClassLoader())) {
				ByteBufferMarshaller marshaller = factory.getMarshaller();
				for (TransactionMode transactionMode : this.transactionModes) {
					for (NearCacheMode nearCacheMode : this.nearCacheModes) {
						for (SessionAttributePersistenceStrategy strategy : EnumSet.allOf(SessionAttributePersistenceStrategy.class)) {
							builder.add(Arguments.of(new HotRodSessionManagerParameters() {
								@Override
								public ByteBufferMarshaller getSessionAttributeMarshaller() {
									return marshaller;
								}

								@Override
								public SessionAttributePersistenceStrategy getSessionAttributePersistenceStrategy() {
									return strategy;
								}

								@Override
								public NearCacheMode getNearCacheMode() {
									return nearCacheMode;
								}

								@Override
								public TransactionMode getTransactionMode() {
									return transactionMode;
								}

								@Override
								public RemoteCacheContainerConfigurator getRemoteCacheContainerConfigurator() {
									return INFINISPAN;
								}

								@Override
								public String toString() {
									return Map.of(ByteBufferMarshaller.class.getSimpleName(), marshaller.toString(), SessionAttributePersistenceStrategy.class.getSimpleName(), strategy).toString();
								}
							}));
						}
					}
				}
			}
			return builder.build();
		}
	}

	static class ConcurrentHotRodSessionManagerArgumentsProvider extends HotRodSessionManagerArgumentsProvider {
		ConcurrentHotRodSessionManagerArgumentsProvider() {
			this.marshallerClass = ProtoStreamTesterFactory.class;
		}
	}

	static class ExpirationHotRodSessionManagerArgumentsProvider extends HotRodSessionManagerArgumentsProvider {
		ExpirationHotRodSessionManagerArgumentsProvider() {
			this.marshallerClass = ProtoStreamTesterFactory.class;
		}
	}

	HotRodSessionManagerITCase() {
		super(HotRodSessionManagerFactoryContext::new);
	}

	@Override
	@ParameterizedTest
	@ArgumentsSource(HotRodSessionManagerArgumentsProvider.class)
	public void basic(HotRodSessionManagerParameters parameters) throws Exception {
		super.basic(parameters);
	}

	@Override
	@ParameterizedTest
	@ArgumentsSource(ConcurrentHotRodSessionManagerArgumentsProvider.class)
	public void concurrent(HotRodSessionManagerParameters parameters) throws Exception {
		super.concurrent(parameters);
	}

	@Override
	@ParameterizedTest
	@ArgumentsSource(ExpirationHotRodSessionManagerArgumentsProvider.class)
	public void expiration(HotRodSessionManagerParameters parameters) throws Exception {
		super.expiration(parameters);
	}
}
