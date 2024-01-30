/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote;

import java.util.EnumSet;
import java.util.stream.Stream;

import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.wildfly.clustering.cache.infinispan.batch.TransactionBatch;
import org.wildfly.clustering.session.SessionAttributePersistenceStrategy;
import org.wildfly.clustering.session.SessionManagerITCase;

/**
 * Session manager integration test using an Infinispan server container.
 * @author Paul Ferraro
 */
public class HotRodSessionManagerITCase extends SessionManagerITCase<TransactionBatch, HotRodSessionManagerParameters> {

	static final InfinispanServerContainer CONTAINER = new InfinispanServerContainer();

	static class HotRodSessionManagerArgumentsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			Stream.Builder<Arguments> builder = Stream.builder();
			for (SessionAttributePersistenceStrategy strategy : EnumSet.allOf(SessionAttributePersistenceStrategy.class)) {
				for (NearCacheMode nearCacheMode : EnumSet.allOf(NearCacheMode.class)) {
					builder.add(Arguments.of(new HotRodSessionManagerParameters() {
						@Override
						public SessionAttributePersistenceStrategy getSessionAttributePersistenceStrategy() {
							return strategy;
						}

						@Override
						public NearCacheMode getNearCacheMode() {
							return nearCacheMode;
						}

						@Override
						public RemoteCacheContainer createRemoteCacheContainer(ConfigurationBuilder builder) {
							return CONTAINER.apply(builder);
						}

						@Override
						public boolean isExpirationDeterministic() {
							return false;
						}
					}));
				}
			}
			return builder.build();
		}
	}

	@BeforeAll
	public static void init() {
		CONTAINER.start();
	}

	@AfterAll
	public static void destroy() {
		CONTAINER.stop();
	}

	HotRodSessionManagerITCase() {
		super(HotRodSessionManagerFactoryProvider::new);
	}

	@ParameterizedTest
	@ArgumentsSource(HotRodSessionManagerArgumentsProvider.class)
	public void basic(HotRodSessionManagerParameters parameters) throws Exception {
		super.basic(parameters);
	}

	@ParameterizedTest
	@ArgumentsSource(HotRodSessionManagerArgumentsProvider.class)
	public void concurrent(HotRodSessionManagerParameters parameters) throws Exception {
		super.concurrent(parameters);
	}

	@ParameterizedTest
	@ArgumentsSource(HotRodSessionManagerArgumentsProvider.class)
	public void expiration(HotRodSessionManagerParameters parameters) throws Exception {
		super.expiration(parameters);
	}
}
