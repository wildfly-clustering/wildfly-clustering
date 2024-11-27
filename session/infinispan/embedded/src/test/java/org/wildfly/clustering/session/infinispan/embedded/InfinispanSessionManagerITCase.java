/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded;

import java.util.EnumSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.transaction.TransactionMode;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.MarshallingTesterFactory;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.session.SessionAttributePersistenceStrategy;
import org.wildfly.clustering.session.cache.SessionManagerITCase;

/**
 * Session manager integration test using an embedded Infinispan cache tested under a combination of settings.
 * @author Paul Ferraro
 */
public class InfinispanSessionManagerITCase extends SessionManagerITCase<InfinispanSessionManagerParameters> {

	static class InfinispanSessionManagerArgumentsProvider implements ArgumentsProvider {
		Class<? extends MarshallingTesterFactory> marshallerClass = MarshallingTesterFactory.class;
		Set<TransactionMode> transactionModes = EnumSet.allOf(TransactionMode.class);
		Set<CacheMode> cacheModes = EnumSet.of(CacheMode.DIST_SYNC, CacheMode.REPL_SYNC);

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			Stream.Builder<Arguments> builder = Stream.builder();
			for (MarshallingTesterFactory factory : ServiceLoader.load(this.marshallerClass, this.marshallerClass.getClassLoader())) {
				ByteBufferMarshaller marshaller = factory.getMarshaller();
				for (SessionAttributePersistenceStrategy strategy : EnumSet.allOf(SessionAttributePersistenceStrategy.class)) {
					for (CacheMode cacheMode : this.cacheModes) {
						for (TransactionMode transactionMode : this.transactionModes) {
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
								public CacheMode getCacheMode() {
									return cacheMode;
								}

								@Override
								public TransactionMode getTransactionMode() {
									return transactionMode;
								}

								@Override
								public String toString() {
									return Map.ofEntries(
											Map.entry(ByteBufferMarshaller.class.getSimpleName(), marshaller.toString()),
											Map.entry(SessionAttributePersistenceStrategy.class.getSimpleName(), strategy.name()),
											Map.entry(CacheMode.class.getSimpleName(), cacheMode.name()),
											Map.entry(TransactionMode.class.getSimpleName(), transactionMode.name())
										).toString();
								}
							}));
						}
					}
				}
			}
			return builder.build();
		}
	}

	static class ConcurrentInfinispanSessionManagerArgumentsProvider extends InfinispanSessionManagerArgumentsProvider {
		ConcurrentInfinispanSessionManagerArgumentsProvider() {
			this.marshallerClass = ProtoStreamTesterFactory.class;
			//this.transactionModes = EnumSet.of(TransactionMode.NON_TRANSACTIONAL);
		}
	}

	static class ExpirationInfinispanSessionManagerArgumentsProvider extends InfinispanSessionManagerArgumentsProvider {
		ExpirationInfinispanSessionManagerArgumentsProvider() {
			this.marshallerClass = ProtoStreamTesterFactory.class;
			this.cacheModes = EnumSet.of(CacheMode.DIST_SYNC);
		}
	}

	InfinispanSessionManagerITCase() {
		super(InfinispanSessionManagerFactoryProvider::new);
	}

	@ParameterizedTest
	@ArgumentsSource(InfinispanSessionManagerArgumentsProvider.class)
	public void basic(InfinispanSessionManagerParameters parameters) throws Exception {
		super.basic(parameters);
	}

	@ParameterizedTest
	@ArgumentsSource(ConcurrentInfinispanSessionManagerArgumentsProvider.class)
	public void concurrent(InfinispanSessionManagerParameters parameters) throws Exception {
		super.concurrent(parameters);
	}

	@ParameterizedTest
	@ArgumentsSource(ExpirationInfinispanSessionManagerArgumentsProvider.class)
	public void expiration(InfinispanSessionManagerParameters parameters) throws Exception {
		super.expiration(parameters);
	}
}
