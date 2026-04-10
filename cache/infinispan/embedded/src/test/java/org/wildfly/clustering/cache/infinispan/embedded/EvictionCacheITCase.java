/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.InstanceOfAssertFactories;
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
			evictable.add(Map.entry(i, Integer.toString(i)));
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
					// Encourage distinct ages
					Thread.sleep(1);
				}
				// Time after which evictable entries will be considered idle
				Instant idleTime = Instant.now().plus(IDLE_THRESHOLD);

				// Verify that nothing was evicted yet, since we are at capacity
				assertThat(entries).isEmpty();

				// Add non-evictable entries - should have no effect on capacity
				for (Map.Entry<Object, String> entry : nonEvictable) {
					cache.put(entry.getKey(), entry.getValue());
				}
				// Verify that nothing was evicted yet, since we are still at capacity
				assertThat(entries).isEmpty();

				// Add excess evictable entries - should trigger synchronous eviction events
				for (Map.Entry<Object, String> entry : evictable.subList(excess, size)) {
					cache.put(entry.getKey(), entry.getValue());
					// Encourage distinct ages
					Thread.sleep(1);
				}

				// Verify that excess evictable entries were evicted according to age
				List<Map.Entry<Object, String>> evictedEntries = new LinkedList<>();
				assertThat(entries.drainTo(evictedEntries)).isEqualTo(excess);
				evictedEntries.forEach(evicted -> assertThat(evicted.getKey()).asInstanceOf(InstanceOfAssertFactories.INTEGER).isLessThan(excess));

				// Read remaining evictable entries so that they are no longer idle
				for (Map.Entry<Object, String> entry : evictable.subList(excess, evictable.size())) {
					assertThat(cache.get(entry.getKey())).isNotNull().isSameAs(entry.getValue());
				}

				// Time after which evictable entries will be considered idle again, plus some grace period
				Instant evictTime = Instant.now().plus(IDLE_THRESHOLD.multipliedBy(4));

				// Allow original idle time to pass
				Thread.sleep(Duration.between(Instant.now(), idleTime).toMillis());

				// Verify nothing was evicted yet
				assertThat(entries.poll()).isNull();

				// Remove non-evictable entries
				for (Map.Entry<Object, String> entry : nonEvictable) {
					assertThat(cache.remove(entry.getKey())).isSameAs(entry.getValue());
				}

				// Verify nothing was evicted yet
				assertThat(entries.poll()).isNull();

				// Verify that idle evictable entries are no longer present and that the corresponding events were fired
				for (Map.Entry<Object, String> entry : evictable.subList(excess, evictable.size())) {
					Map.Entry<Object, String> evictedEntry = entries.poll(Duration.between(Instant.now(), evictTime).toNanos(), TimeUnit.NANOSECONDS);
					assertThat(evictedEntry).isNotNull();
					assertThat(evictedEntry.getValue()).isSameAs(entry.getValue());

					assertThat(cache.get(entry.getKey())).isNull();
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
	private static class EvictionEventListener<K, V> {
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
