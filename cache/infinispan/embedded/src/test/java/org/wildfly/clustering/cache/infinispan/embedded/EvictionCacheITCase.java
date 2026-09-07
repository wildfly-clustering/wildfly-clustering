/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

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
	private static final Duration IDLE_THRESHOLD = Duration.ofSeconds(2);
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
			Cache<Object, String> cache = new AdvancedCacheDecorator<>(manager.<Object, String>getCache(cacheName).getAdvancedCache(), UnaryOperator.identity());
			cache.start();
			BlockingQueue<Map.Entry<Object, String>> evictedEntries = new LinkedBlockingQueue<>();
			Object listener = new EvictionEventListener<>(evictedEntries);
			cache.addListener(listener);
			try {
				// Add evictable entries to capacity
				for (Map.Entry<Object, String> entry : evictable.subList(0, excess)) {
					cache.put(entry.getKey(), entry.getValue());
					// Encourage distinct ages
					Thread.sleep(1);
				}

				// Verify that nothing was evicted yet, since we are at capacity
				assertThat(evictedEntries).isEmpty();

				// Add non-evictable entries - should have no effect on capacity
				for (Map.Entry<Object, String> entry : nonEvictable) {
					cache.put(entry.getKey(), entry.getValue());
				}
				// Verify that nothing was evicted yet, since we are still at capacity
				assertThat(evictedEntries).isEmpty();

				// Add excess evictable entries - should trigger synchronous eviction events
				for (Map.Entry<Object, String> entry : evictable.subList(excess, size)) {
					cache.put(entry.getKey(), entry.getValue());
					// Encourage distinct ages
					Thread.sleep(1);
				}

				// Verify that excess evictable entries were evicted according to age
				List<Map.Entry<Object, String>> entries = new ArrayList<>(excess);
				assertThat(evictedEntries.drainTo(entries)).isEqualTo(excess);
				assertThat(entries).containsExactlyInAnyOrderElementsOf(evictable.subList(0, excess));

				List<Map.Entry<Object, String>> remainingEvictable = evictable.subList(excess, evictable.size());
				Duration activeDelay = IDLE_THRESHOLD.dividedBy(2L);

				// Verify cache reads via Cache.get() defer eviction
				for (int i = 0; i < 4; ++i) {
					// Read remaining evictable entries so that they are no longer idle
					for (Map.Entry<Object, String> entry : remainingEvictable) {
						assertThat(cache.get(entry.getKey())).isNotNull().isSameAs(entry.getValue());
					}

					Thread.sleep(activeDelay.toMillis());

					// Verify nothing else was evicted yet
					assertThat(evictedEntries.poll()).isNull();
				}

				// Verify cache reads via Cache.putIfAbsent(...) defer eviction
				for (int i = 0; i < 4; ++i) {
					// Read remaining evictable entries so that they are no longer idle
					for (Map.Entry<Object, String> entry : remainingEvictable) {
						assertThat(cache.putIfAbsent(entry.getKey(), "")).isSameAs(entry.getValue());
					}

					Thread.sleep(activeDelay.toMillis());

					// Verify nothing else was evicted yet
					assertThat(evictedEntries.poll()).isNull();
				}

				// Verify cache reads via Cache.computeIfAbsent(...) defer eviction
				for (int i = 0; i < 4; ++i) {
					// Read remaining evictable entries so that they are no longer idle
					for (Map.Entry<Object, String> entry : remainingEvictable) {
						assertThat(cache.computeIfAbsent(entry.getKey(), v -> "")).isSameAs(entry.getValue());
					}

					Thread.sleep(activeDelay.toMillis());

					// Verify nothing else was evicted yet
					assertThat(evictedEntries.poll()).isNull();
				}

				entries.clear();
				Duration delay = IDLE_THRESHOLD.multipliedBy(3);
				for (int i = 0; i < remainingEvictable.size(); ++i) {
					Map.Entry<Object, String> evictedEntry = evictedEntries.poll(delay.toMillis(), TimeUnit.MILLISECONDS);
					assertThat(evictedEntry).isNotNull();
					entries.add(evictedEntry);
					delay = activeDelay;
				}

				// Verify that idle evictable entries are no longer present and that the corresponding events were fired
				assertThat(entries).containsExactlyInAnyOrderElementsOf(remainingEvictable);

				// Cache should only contain non-evictable entries
				assertThat(cache.size()).isEqualTo(nonEvictable.size());
				for (Map.Entry<Object, String> entry : nonEvictable) {
					assertThat(cache.get(entry.getKey())).isSameAs(entry.getValue());
				}

				// Verify nothing else was evicted
				assertThat(evictedEntries.poll()).isNull();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				cache.removeListener(listener);
				cache.clear();
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
