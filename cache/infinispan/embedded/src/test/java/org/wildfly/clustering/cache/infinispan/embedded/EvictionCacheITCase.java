/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheType;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntriesEvicted;
import org.infinispan.notifications.cachelistener.event.CacheEntriesEvictedEvent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.wildfly.clustering.cache.infinispan.embedded.container.DataContainerConfigurationBuilder;
import org.wildfly.clustering.context.Context;

/**
 * Integration test that validates eviction configured via {@link org.wildfly.clustering.cache.infinispan.embedded.container.DataContainerConfiguration}.
 * Validates all cache type to include coverage of both segmented and non-segmented data containers.
 * @author Paul Ferraro
 */
public class EvictionCacheITCase {
	private static final String CLUSTER_NAME = "ISPN";
	private static final int CAPACITY = 10;
	private static final Duration IDLE_THRESHOLD = Duration.ofSeconds(1);
	private static final Class<?> EVICTABLE = Integer.class;

	@ParameterizedTest
	@EnumSource(CacheType.class)
	public void test(CacheType type) {
		String cacheName = type.name();
		int excess = CAPACITY;
		int size = CAPACITY + excess;
		List<Map.Entry<Object, String>> nonEvictable = new ArrayList<>(CAPACITY);
		for (int i = 0; i < CAPACITY; ++i) {
			nonEvictable.add(Map.entry(Integer.toString(i), Integer.toString(i)));
		}
		List<Map.Entry<Object, String>> evictable = new ArrayList<>(size);
		for (int i = 0; i < size; ++i) {
			evictable.add(Map.entry(Integer.valueOf(i), Integer.toString(i)));
		}

		ConfigurationBuilder builder = new ConfigurationBuilder();
		DataContainerConfigurationBuilder containerBuilder = builder.addModule(DataContainerConfigurationBuilder.class);
		containerBuilder.evictable(EVICTABLE::isInstance).idleTimeout(IDLE_THRESHOLD);
		Configuration configuration = builder.clustering().cacheType(type).memory().maxCount(CAPACITY).whenFull(EvictionStrategy.REMOVE).build();
		try (Context<EmbeddedCacheManager> context = new EmbeddedCacheManagerContext(CLUSTER_NAME, "member1")) {
			EmbeddedCacheManager manager = context.get();
			manager.defineConfiguration(cacheName, configuration);
			Cache<Object, String> cache = manager.getCache(cacheName);
			cache.start();
			BlockingQueue<Map.Entry<Object, String>> entries = new LinkedBlockingDeque<>();
			Object listener = new EvictionEventListener<>(entries);
			cache.addListener(listener);
			try {
				// Add evictable entries to capacity
				for (Map.Entry<Object, String> entry : evictable.subList(0, excess)) {
					cache.put(entry.getKey(), entry.getValue());
				}
				// Verify that capacity was not exceeded
				assertThat(entries).isEmpty();
				// Add non-evictable entries - should have no effect on capacity
				for (Map.Entry<Object, String> entry : nonEvictable) {
					cache.put(entry.getKey(), entry.getValue());
				}
				// Verify that capacity was not exceeded
				assertThat(entries).isEmpty();

				Instant start = Instant.now();
				// Add excess evictable entries - should trigger synchronous eviction events
				for (Map.Entry<Object, String> entry : evictable.subList(excess, size)) {
					cache.put(entry.getKey(), entry.getValue());
				}
				Instant evictTime = Instant.now().plus(IDLE_THRESHOLD).plus(IDLE_THRESHOLD);

				// Verify that excess evictable entries were evicted
				for (int i = 0; i < excess; ++i) {
					Map.Entry<Object, String> entry = entries.poll();
					assertThat(entry).isNotNull();
					assertThat(entry.getKey()).isInstanceOf(EVICTABLE);
				}
				// Verify nothing else was evicted
				assertThat(entries.poll()).isNull();

				// Remove non-evictable entries
				for (Map.Entry<Object, String> entry : nonEvictable) {
					assertThat(cache.remove(entry.getKey())).isSameAs(entry.getValue());
				}

				// Verify that idle evictable entries are eventually evicted
				for (int i = 0; i < CAPACITY; ++i) {
					Map.Entry<Object, String> entry = entries.poll(Duration.between(Instant.now(), evictTime).toNanos(), TimeUnit.NANOSECONDS);
					assertThat(entry).isNotNull();
					assertThat(entry.getKey()).isInstanceOf(EVICTABLE);
					if (i == 0) {
						// Verify that we do not evict prematurely
						assertThat(Duration.between(start, Instant.now())).isGreaterThanOrEqualTo(IDLE_THRESHOLD);
					}
				}
				// Verify nothing else was evicted
				assertThat(entries.poll()).isNull();

				// Cache should be empty at this point
				for (Map.Entry<Object, String> entry : evictable) {
					assertThat(cache.get(entry.getKey())).isNull();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				cache.addListener(listener);
				cache.stop();
			}
		}
	}

	@Listener
	private class EvictionEventListener<K, V> {
		private final Queue<Map.Entry<K, V>> entries;

		EvictionEventListener(Queue<Map.Entry<K, V>> entries) {
			this.entries = entries;
		}

		@CacheEntriesEvicted
		public void cacheEntriesEvicted(CacheEntriesEvictedEvent<K, V> event) {
			if (!event.isPre()) {
				event.getEntries().entrySet().forEach(this.entries::add);
			}
		}
	}
}
