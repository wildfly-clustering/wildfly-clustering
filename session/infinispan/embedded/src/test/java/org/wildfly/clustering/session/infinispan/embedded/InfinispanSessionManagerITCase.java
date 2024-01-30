/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded;

import java.util.EnumSet;
import java.util.stream.Stream;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.transaction.TransactionMode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.wildfly.clustering.cache.infinispan.batch.TransactionBatch;
import org.wildfly.clustering.session.SessionAttributePersistenceStrategy;
import org.wildfly.clustering.session.SessionManagerITCase;

/**
 * Session manager integration test using an embedded Infinispan cache tested under a combination of settings.
 * @author Paul Ferraro
 */
public class InfinispanSessionManagerITCase extends SessionManagerITCase<TransactionBatch, InfinispanSessionManagerParameters> {

	static class InfinispanSessionManagerArgumentsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			Stream.Builder<Arguments> builder = Stream.builder();
			for (SessionAttributePersistenceStrategy strategy : EnumSet.allOf(SessionAttributePersistenceStrategy.class)) {
				for (CacheMode cacheMode : EnumSet.of(CacheMode.DIST_SYNC, CacheMode.REPL_SYNC)) {
					for (TransactionMode transactionMode : EnumSet.allOf(TransactionMode.class)) {
						builder.add(Arguments.of(new InfinispanSessionManagerParameters() {
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
							public boolean isExpirationDeterministic() {
								return true;
							}
						}));
					}
				}
			}
			return builder.build();
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

	@Disabled
	@ParameterizedTest
	@ArgumentsSource(InfinispanSessionManagerArgumentsProvider.class)
	public void expiration(InfinispanSessionManagerParameters parameters) throws Exception {
		super.expiration(parameters);
	}
}