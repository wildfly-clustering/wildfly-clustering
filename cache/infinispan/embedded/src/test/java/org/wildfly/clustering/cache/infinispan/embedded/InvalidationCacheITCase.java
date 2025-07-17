/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiFunction;

import jakarta.transaction.TransactionManager;

import org.assertj.core.api.SoftAssertions;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheType;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.TransactionConfigurationBuilder;
import org.infinispan.context.Flag;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryInvalidated;
import org.infinispan.notifications.cachelistener.event.CacheEntryInvalidatedEvent;
import org.infinispan.persistence.jdbc.common.DatabaseType;
import org.infinispan.persistence.jdbc.configuration.JdbcStringBasedStoreConfigurationBuilder;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.function.UnaryOperator;

/**
 * Validates behavior of write operations on an invalidation-cache.
 * @author Paul Ferraro
 */
public class InvalidationCacheITCase extends SoftAssertions {
	private static final String CLUSTER_NAME = "ISPN";
	private static final UnaryOperator<TransactionConfigurationBuilder> NON_TRANSACTIONAL = UnaryOperator.identity();
	private static final UnaryOperator<TransactionConfigurationBuilder> TRANSACTIONAL = builder -> builder
			.transactionMode(org.infinispan.transaction.TransactionMode.TRANSACTIONAL)
			.transactionManagerLookup(org.infinispan.transaction.tm.EmbeddedTransactionManager::getInstance)
			.lockingMode(org.infinispan.transaction.LockingMode.PESSIMISTIC)
			;

	@Test
	public void nonTxInvalidation() throws Exception {
		this.test("non-tx", NON_TRANSACTIONAL, UnaryOperator.identity());
	}

	@org.junit.jupiter.api.Disabled("Currently fails due to missing invalidations")
	@Test
	public void txInvalidation() throws Exception {
		this.test("tx", TRANSACTIONAL, UnaryOperator.identity());
	}

	private void test(String cacheName, UnaryOperator<TransactionConfigurationBuilder> configurator, UnaryOperator<Cache<String, Integer>> transformer) throws Exception {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		Configuration configuration = configurator.apply(builder.transaction())
				.clustering().cacheType(CacheType.INVALIDATION)
				.persistence().addStore(JdbcStringBasedStoreConfigurationBuilder.class).dialect(DatabaseType.H2).table().createOnStart(true).tableNamePrefix("ispn").idColumnName("id").idColumnType("VARCHAR").dataColumnName("data").dataColumnType("VARBINARY").segmentColumnName("segment").segmentColumnType("NUMERIC").timestampColumnName("ts").timestampColumnType("BIGINT").simpleConnection().driverClass("org.h2.Driver").connectionUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1").username("sa").password("").shared(true)
				.build();
		try (Context<EmbeddedCacheManager> manager1Context = new EmbeddedCacheManagerContext(CLUSTER_NAME, "member1")) {
			EmbeddedCacheManager manager1 = manager1Context.get();
			manager1.defineConfiguration(cacheName, configuration);
			Cache<String, Integer> cache1 = transformer.apply(manager1.getCache(cacheName));
			cache1.start();

			try (Context<EmbeddedCacheManager> manager2Context = new EmbeddedCacheManagerContext(CLUSTER_NAME, "member2")) {
				EmbeddedCacheManager manager2 = manager2Context.get();
				manager2.defineConfiguration(cacheName, configuration);
				Cache<String, Integer> cache2 = transformer.apply(manager2.getCache(cacheName));
				cache2.start();

				try {
					this.testPut(cache1, cache2);
					this.testPutIgnoreReturnValue(cache1.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES), cache2.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES));
					this.testCompute(cache1, cache2);
					this.testComputeIgnoreReturnValue(cache1.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES), cache2.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES));

					assertAll();
				} finally {
					cache2.stop();
				}
			} finally {
				cache1.stop();
			}
		}
	}

	private void testPut(Cache<String, Integer> cache1, Cache<String, Integer> cache2) throws Exception {
		TransactionManager tm1 = cache1.getAdvancedCache().getTransactionManager();
		TransactionManager tm2 = cache2.getAdvancedCache().getTransactionManager();
		Cache<String, Integer> skipLoadCache1 = cache1.getAdvancedCache().withFlags(Flag.SKIP_CACHE_LOAD);
		Cache<String, Integer> skipLoadCache2 = cache2.getAdvancedCache().withFlags(Flag.SKIP_CACHE_LOAD);
		Queue<Map.Entry<String, Listener.Observation>> events1 = new LinkedBlockingQueue<>();
		Queue<Map.Entry<String, Listener.Observation>> events2 = new LinkedBlockingQueue<>();
		Object listener1 = new InvalidationEventCollector(events1);
		Object listener2 = new InvalidationEventCollector(events2);
		String key = "put";
		cache1.addListener(listener1);
		cache2.addListener(listener2);
		try {
			if (tm1 != null) {
				tm1.begin();
			}
			// Initial write
			assertThat(cache1.putIfAbsent(key, 0)).isNull();
			if (tm1 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm1.commit();
			}
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events2.poll()).isNull();
			// Entry should only exist locally
			assertThat(skipLoadCache1.get(key)).isEqualTo(0);
			assertThat(skipLoadCache2.get(key)).isNull();

			if (tm2 != null) {
				tm2.begin();
			}
			// Requires loading of current value, but nothing should be written
			assertThat(cache2.putIfAbsent(key, -1)).isEqualTo(0);
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			// Nothing was updated, there should be no invalidation events
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();
			// Entry should have been loaded locally but not invalidated remotely since nothing was written
			assertThat(skipLoadCache1.get(key)).isEqualTo(0);
			assertThat(skipLoadCache2.get(key)).isEqualTo(0);

			if (tm2 != null) {
				tm2.begin();
			}
			// Write requires previous value, already available locally
			assertThat(cache2.put(key, 1)).isEqualTo(0);
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();
			// Verify that entry was invalidated remotely
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache2.get(key)).isEqualTo(1);

			if (tm1 != null) {
				tm1.begin();
			}
			// Write requires loading current value
			assertThat(cache1.put(key, 2)).isEqualTo(1);
			if (tm1 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm1.commit();
			}
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events2.poll()).isNull();
			// Verify that entry was invalidated remotely
			assertThat(skipLoadCache1.get(key)).isEqualTo(2);
			assertThat(skipLoadCache2.get(key)).isNull();

			if (tm2 != null) {
				tm2.begin();
			}
			// Write requires loading of current value, to be replaced
			assertThat(cache2.replace(key, 3)).isEqualTo(2);
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();
			// Verify entry was invalidated remotely
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache2.get(key)).isEqualTo(3);

			if (tm2 != null) {
				tm2.begin();
			}
			// Current value already available locally, to be replaced
			assertThat(cache2.replace(key, 4)).isEqualTo(3);
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();
			// Entry would already have been invalidated remotely
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache2.get(key)).isEqualTo(4);

			if (tm1 != null) {
				tm1.begin();
			}
			// Requires loading current value, to be removed
			assertThat(cache1.remove(key)).isEqualTo(4);
			if (tm1 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm1.commit();
			}
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events2.poll()).isNull();

			// Verify entry was invalidated remotely
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache1.get(key)).isNull();

			if (tm2 != null) {
				tm2.begin();
			}
			// Requires store lookup, nothing written
			assertThat(cache2.replace(key, 0)).isNull();
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			// Nothing to invalidate
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();

			// Verify no entry in memory
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache1.get(key)).isNull();
			// Verify no entry in store
			assertThat(cache1.get(key)).isNull();
			assertThat(cache2.get(key)).isNull();
		} finally {
			cache1.removeListener(listener1);
			cache2.removeListener(listener2);
		}
	}

	private void testPutIgnoreReturnValue(Cache<String, Integer> cache1, Cache<String, Integer> cache2) throws Exception {
		TransactionManager tm1 = cache1.getAdvancedCache().getTransactionManager();
		TransactionManager tm2 = cache2.getAdvancedCache().getTransactionManager();
		Cache<String, Integer> skipLoadCache1 = cache1.getAdvancedCache().withFlags(Flag.SKIP_CACHE_LOAD);
		Cache<String, Integer> skipLoadCache2 = cache2.getAdvancedCache().withFlags(Flag.SKIP_CACHE_LOAD);
		Queue<Map.Entry<String, Listener.Observation>> events1 = new LinkedBlockingQueue<>();
		Queue<Map.Entry<String, Listener.Observation>> events2 = new LinkedBlockingQueue<>();
		Object listener1 = new InvalidationEventCollector(events1);
		Object listener2 = new InvalidationEventCollector(events2);
		String key = "put-ignore";
		cache1.addListener(listener1);
		cache2.addListener(listener2);
		try {
			if (tm1 != null) {
				tm1.begin();
			}
			// Initial write
			assertThat(cache1.putIfAbsent(key, 0)).isNull();
			if (tm1 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm1.commit();
			}
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events2.poll()).isNull();
			// Entry should only exist locally
			assertThat(skipLoadCache1.get(key)).isEqualTo(0);
			assertThat(skipLoadCache2.get(key)).isNull();

			if (tm2 != null) {
				tm2.begin();
			}
			// Requires loading current value, but nothing will be written
			assertThat(cache2.putIfAbsent(key, -1)).satisfiesAnyOf(result -> assertThat(result).isNull(), result -> assertThat(result).isEqualTo(0));
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			// Nothing was updated, there should be no invalidation events
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();
			// Entry should have been loaded locally but not invalidated remotely since nothing was written
			assertThat(skipLoadCache1.get(key)).isEqualTo(0);
			assertThat(skipLoadCache2.get(key)).isEqualTo(0);

			if (tm2 != null) {
				tm2.begin();
			}
			// Write does not require current value
			assertThat(cache2.put(key, 1)).satisfiesAnyOf(result -> assertThat(result).isNull(), result -> assertThat(result).isEqualTo(0));
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();
			// Verify entry was invalidated remotely
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache2.get(key)).isEqualTo(1);

			if (tm1 != null) {
				tm1.begin();
			}
			// Write does not requiring loading current value
			assertThat(cache1.put(key, 2)).satisfiesAnyOf(result -> assertThat(result).isNull(), result -> assertThat(result).isEqualTo(1));
			if (tm1 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm1.commit();
			}
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events2.poll()).isNull();
			// Verify entry was invalidated remotely
			assertThat(skipLoadCache1.get(key)).isEqualTo(2);
			assertThat(skipLoadCache2.get(key)).isNull();

			if (tm2 != null) {
				tm2.begin();
			}
			// Write requires loading current value
			assertThat(cache2.replace(key, 3)).satisfiesAnyOf(result -> assertThat(result).isNull(), result -> assertThat(result).isEqualTo(2));
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();
			// Verify entry was invalidated remotely
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache2.get(key)).isEqualTo(3);

			if (tm2 != null) {
				tm2.begin();
			}
			// Write does not require loading since current value available locally
			assertThat(cache2.replace(key, 4)).satisfiesAnyOf(result -> assertThat(result).isNull(), result -> assertThat(result).isEqualTo(3));
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();
			// Remote entry would already have been invalidated
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache2.get(key)).isEqualTo(4);

			if (tm1 != null) {
				tm1.begin();
			}
			// Write does not require loading current value
			assertThat(cache1.remove(key)).satisfiesAnyOf(result -> assertThat(result).isNull(), result -> assertThat(result).isEqualTo(4));
			if (tm1 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm1.commit();
			}
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events2.poll()).isNull();
			// Verify entry was invalidated remotely
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache1.get(key)).isNull();
			// Verify store removal
			assertThat(cache1.get(key)).isNull();
			assertThat(cache2.get(key)).isNull();

			if (tm2 != null) {
				tm2.begin();
			}
			// Requires store lookup, nothing written
			assertThat(cache2.replace(key, 0)).isNull();
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			// Nothing to invalidate
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();

			// Verify no entry in memory
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache1.get(key)).isNull();
			// Verify no entry in store
			assertThat(cache1.get(key)).isNull();
			assertThat(cache2.get(key)).isNull();
		} finally {
			cache1.removeListener(listener1);
			cache2.removeListener(listener2);
		}
	}

	private void testCompute(Cache<String, Integer> cache1, Cache<String, Integer> cache2) throws Exception {
		TransactionManager tm1 = cache1.getAdvancedCache().getTransactionManager();
		TransactionManager tm2 = cache2.getAdvancedCache().getTransactionManager();
		Cache<String, Integer> skipLoadCache1 = cache1.getAdvancedCache().withFlags(Flag.SKIP_CACHE_LOAD);
		Cache<String, Integer> skipLoadCache2 = cache2.getAdvancedCache().withFlags(Flag.SKIP_CACHE_LOAD);
		Queue<Map.Entry<String, Listener.Observation>> events1 = new LinkedBlockingQueue<>();
		Queue<Map.Entry<String, Listener.Observation>> events2 = new LinkedBlockingQueue<>();
		Object listener1 = new InvalidationEventCollector(events1);
		Object listener2 = new InvalidationEventCollector(events2);
		String key = "compute";
		cache1.addListener(listener1);
		cache2.addListener(listener2);
		try {
			BiFunction<String, Integer, Integer> increment = (k, v) -> (v != null) ? v + 1 : 0;

			if (tm1 != null) {
				tm1.begin();
			}
			// Initial write
			assertThat(cache1.computeIfAbsent(key, k -> 0)).isEqualTo(0);
			if (tm1 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm1.commit();
			}
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events2.poll()).isNull();
			// Entry should only exist locally
			assertThat(skipLoadCache1.get(key)).isEqualTo(0);
			assertThat(skipLoadCache2.get(key)).isNull();

			if (tm2 != null) {
				tm2.begin();
			}
			// Requires loading current value, but nothing written
			assertThat(cache2.computeIfAbsent(key, k -> -1)).isEqualTo(0);
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			// Nothing was updated, there should be no invalidation events
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();
			// Value should still be available remotely as nothing was written/invalidated
			assertThat(skipLoadCache1.get(key)).isEqualTo(0);
			assertThat(skipLoadCache2.get(key)).isEqualTo(0);

			if (tm2 != null) {
				tm2.begin();
			}
			// Write requires current value already available locally
			assertThat(cache2.compute(key, increment)).isEqualTo(1);
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();
			// Verify entry was invalidated remotely
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache2.get(key)).isEqualTo(1);

			if (tm1 != null) {
				tm1.begin();
			}
			// Write requires loading current value
			assertThat(cache1.compute(key, increment)).isEqualTo(2);
			if (tm1 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm1.commit();
			}
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events2.poll()).isNull();
			// Verify entry was invalidated remotely
			assertThat(skipLoadCache1.get(key)).isEqualTo(2);
			assertThat(skipLoadCache2.get(key)).isNull();

			if (tm2 != null) {
				tm2.begin();
			}
			// Write requires loading current value
			assertThat(cache2.computeIfPresent(key, increment)).isEqualTo(3);
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();
			// Verify entry was invalidated remotely
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache2.get(key)).isEqualTo(3);

			if (tm2 != null) {
				tm2.begin();
			}
			// Write requires current value, already available locally
			assertThat(cache2.computeIfPresent(key, increment)).isEqualTo(4);
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();
			// Entry was already invalidated remotely
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache2.get(key)).isEqualTo(4);

			if (tm1 != null) {
				tm1.begin();
			}
			// Write requires loading current value
			assertThat(cache1.computeIfPresent(key, (k, v) -> null)).isNull();
			if (tm1 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm1.commit();
			}
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events2.poll()).isNull();
			// Verify entry was invalidated remotely
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache1.get(key)).isNull();
			// Verify store removal
			assertThat(cache1.get(key)).isNull();
			assertThat(cache2.get(key)).isNull();

			if (tm2 != null) {
				tm2.begin();
			}
			// Requires store lookup, nothing written
			assertThat(cache2.computeIfPresent(key, (k, v) -> 0)).isNull();
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			// Nothing to invalidate
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();

			// Verify no entry in memory
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache1.get(key)).isNull();
			// Verify no entry in store
			assertThat(cache1.get(key)).isNull();
			assertThat(cache2.get(key)).isNull();
		} finally {
			cache1.removeListener(listener1);
			cache2.removeListener(listener2);
		}
	}

	private void testComputeIgnoreReturnValue(Cache<String, Integer> cache1, Cache<String, Integer> cache2) throws Exception {
		TransactionManager tm1 = cache1.getAdvancedCache().getTransactionManager();
		TransactionManager tm2 = cache2.getAdvancedCache().getTransactionManager();
		Cache<String, Integer> skipLoadCache1 = cache1.getAdvancedCache().withFlags(Flag.SKIP_CACHE_LOAD);
		Cache<String, Integer> skipLoadCache2 = cache2.getAdvancedCache().withFlags(Flag.SKIP_CACHE_LOAD);
		Queue<Map.Entry<String, Listener.Observation>> events1 = new LinkedBlockingQueue<>();
		Queue<Map.Entry<String, Listener.Observation>> events2 = new LinkedBlockingQueue<>();
		Object listener1 = new InvalidationEventCollector(events1);
		Object listener2 = new InvalidationEventCollector(events2);
		String key = "compute-ignore";
		cache1.addListener(listener1);
		cache2.addListener(listener2);
		try {
			BiFunction<String, Integer, Integer> increment = (k, v) -> (v != null) ? v + 1 : 0;

			if (tm1 != null) {
				tm1.begin();
			}
			// Initial write
			assertThat(cache1.computeIfAbsent(key, k -> 0)).satisfiesAnyOf(result -> assertThat(result).isNull(), result -> assertThat(result).isEqualTo(0));
			if (tm1 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm1.commit();
			}
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events2.poll()).isNull();
			// Entry should only exist locally
			assertThat(skipLoadCache1.get(key)).isEqualTo(0);
			assertThat(skipLoadCache2.get(key)).isNull();

			if (tm2 != null) {
				tm2.begin();
			}
			// Write requires loading current value, but nothing should be written
			assertThat(cache2.computeIfAbsent(key, k -> -1)).satisfiesAnyOf(result -> assertThat(result).isNull(), result -> assertThat(result).isEqualTo(0));
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			// Nothing was updated, there should be no invalidation events
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();
			// Entry should still be available remotely since nothing was invalidated
			assertThat(skipLoadCache1.get(key)).isEqualTo(0);
			assertThat(skipLoadCache2.get(key)).isEqualTo(0);

			if (tm2 != null) {
				tm2.begin();
			}
			// Write requires current value already available locally
			assertThat(cache2.compute(key, increment)).satisfiesAnyOf(result -> assertThat(result).isNull(), result -> assertThat(result).isEqualTo(1));
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();
			// Verify entry was invalidated remotely
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache2.get(key)).isEqualTo(1);

			if (tm1 != null) {
				tm1.begin();
			}
			// Write requires current value already available locally
			assertThat(cache1.compute(key, increment)).satisfiesAnyOf(result -> assertThat(result).isNull(), result -> assertThat(result).isEqualTo(2));
			if (tm1 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm1.commit();
			}
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events2.poll()).isNull();
			// Entry already invalidated
			assertThat(skipLoadCache1.get(key)).isEqualTo(2);
			assertThat(skipLoadCache2.get(key)).isNull();

			if (tm2 != null) {
				tm2.begin();
			}
			// Write requires loading current value
			assertThat(cache2.computeIfPresent(key, increment)).satisfiesAnyOf(result -> assertThat(result).isNull(), result -> assertThat(result).isEqualTo(3));
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();
			// Verify entry was invalidated remotely
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache2.get(key)).isEqualTo(3);

			if (tm2 != null) {
				tm2.begin();
			}
			// Write required current value, already available locally
			assertThat(cache2.computeIfPresent(key, increment)).satisfiesAnyOf(result -> assertThat(result).isNull(), result -> assertThat(result).isEqualTo(4));
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events1.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();
			// Entry already invalidated
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache2.get(key)).isEqualTo(4);

			if (tm1 != null) {
				tm1.begin();
			}
			// Write requires loading current value
			assertThat(cache1.computeIfPresent(key, (k, v) -> null)).isNull();
			if (tm1 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm1.commit();
			}
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.PRE));
			assertThat(events2.poll()).isEqualTo(Map.entry(key, Listener.Observation.POST));
			assertThat(events2.poll()).isNull();
			// Verify entry was invalidated remotely
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache1.get(key)).isNull();
			// Verify store removal
			assertThat(cache1.get(key)).isNull();
			assertThat(cache2.get(key)).isNull();

			if (tm2 != null) {
				tm2.begin();
			}
			// Requires store lookup, nothing written
			assertThat(cache2.computeIfPresent(key, (k, v) -> 0)).isNull();
			if (tm2 != null) {
				// No invalidation events expected before commit
				assertThat(events1.poll()).isNull();
				assertThat(events2.poll()).isNull();
				tm2.commit();
			}
			// Nothing to invalidate
			assertThat(events1.poll()).isNull();
			assertThat(events2.poll()).isNull();

			// Verify no entry in memory
			assertThat(skipLoadCache1.get(key)).isNull();
			assertThat(skipLoadCache1.get(key)).isNull();
			// Verify no entry in store
			assertThat(cache1.get(key)).isNull();
			assertThat(cache2.get(key)).isNull();
		} finally {
			cache1.removeListener(listener1);
			cache2.removeListener(listener2);
		}
	}

	@Listener
	public class InvalidationEventCollector {
		private final Collection<Map.Entry<String, Listener.Observation>> events;

		InvalidationEventCollector(Collection<Map.Entry<String, Listener.Observation>> events) {
			this.events = events;
		}

		@CacheEntryInvalidated
		public void invalidated(CacheEntryInvalidatedEvent<String, Integer> event) {
			this.events.add(Map.entry(event.getKey(), event.isPre() ? Listener.Observation.PRE : Listener.Observation.POST));
		}
	}
}
