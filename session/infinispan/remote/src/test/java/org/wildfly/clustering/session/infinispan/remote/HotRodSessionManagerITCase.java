/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote;

import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Stream;

import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.wildfly.clustering.cache.infinispan.batch.TransactionBatch;
import org.wildfly.clustering.cache.infinispan.remote.InfinispanServerExtension;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheContainerConfigurator;
import org.wildfly.clustering.session.SessionAttributePersistenceStrategy;
import org.wildfly.clustering.session.cache.SessionManagerITCase;

/**
 * Session manager integration test using an Infinispan server container.
 * @author Paul Ferraro
 */
public class HotRodSessionManagerITCase extends SessionManagerITCase<TransactionBatch, HotRodSessionManagerParameters> {

	@RegisterExtension
	static final InfinispanServerExtension INFINISPAN = new InfinispanServerExtension();

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
						public RemoteCacheContainerConfigurator getRemoteCacheContainerConfigurator() {
							return INFINISPAN;
						}

						@Override
						public String toString() {
							return Map.of(SessionAttributePersistenceStrategy.class.getSimpleName(), strategy, NearCacheMode.class.getSimpleName(), nearCacheMode).toString();
						}
					}));
				}
			}
			return builder.build();
		}
	}

	static class NearCacheDisabledHotRodSessionManagerArgumentsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			Stream.Builder<Arguments> builder = Stream.builder();
			for (SessionAttributePersistenceStrategy strategy : EnumSet.allOf(SessionAttributePersistenceStrategy.class)) {
				builder.add(Arguments.of(new HotRodSessionManagerParameters() {
					@Override
					public SessionAttributePersistenceStrategy getSessionAttributePersistenceStrategy() {
						return strategy;
					}

					@Override
					public NearCacheMode getNearCacheMode() {
						return NearCacheMode.DISABLED;
					}

					@Override
					public RemoteCacheContainerConfigurator getRemoteCacheContainerConfigurator() {
						return INFINISPAN;
					}

					@Override
					public String toString() {
						return Map.of(SessionAttributePersistenceStrategy.class.getSimpleName(), strategy).toString();
					}
				}));
			}
			return builder.build();
		}
	}

	HotRodSessionManagerITCase() {
		super(HotRodSessionManagerFactoryProvider::new);
	}

	@ParameterizedTest(name = "{arguments}")
	@ArgumentsSource(NearCacheDisabledHotRodSessionManagerArgumentsProvider.class)
	public void basic(HotRodSessionManagerParameters parameters) throws Exception {
		super.basic(parameters);
	}

	@ParameterizedTest(name = "{arguments}")
	@ArgumentsSource(HotRodSessionManagerArgumentsProvider.class)
	public void concurrent(HotRodSessionManagerParameters parameters) throws Exception {
		super.concurrent(parameters);
	}

	@ParameterizedTest(name = "{arguments}")
	@ArgumentsSource(HotRodSessionManagerArgumentsProvider.class)
	public void expiration(HotRodSessionManagerParameters parameters) throws Exception {
		super.expiration(parameters);
	}
}
