/*
 * Copyright The/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheType;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryActivated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryPassivated;
import org.infinispan.notifications.cachelistener.event.CacheEntryActivatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryPassivatedEvent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.wildfly.clustering.cache.infinispan.embedded.container.DataContainerConfigurationBuilder;
import org.wildfly.clustering.context.Context;

/**
 * Integration test that validates eviction configured via {@link org.wildfly.clustering.cache.infinispan.embedded.container.DataContainerConfiguration}.
 * Validates all cache type to include coverage of both segmented and non-segmented data containers.
 * @author Paul Ferraro
 */
public class PassivationCacheITCase {
	private static final String CLUSTER_NAME = "ISPN";
	private static final Class<?> EVICTABLE = UUID.class;

	@ParameterizedTest
	@EnumSource(CacheType.class)
	public void test(CacheType type) {
		String cacheName = type.name();

		ConfigurationBuilder builder = new ConfigurationBuilder();
		DataContainerConfigurationBuilder containerBuilder = builder.addModule(DataContainerConfigurationBuilder.class);
		containerBuilder.evictable(EVICTABLE::isInstance).idleTimeout(Duration.ofSeconds(1));
		Configuration configuration = builder.clustering().cacheType(type).memory().maxCount(10).whenFull(EvictionStrategy.REMOVE).persistence().passivation(true).addSoftIndexFileStore().build();
		try (Context<EmbeddedCacheManager> context = new EmbeddedCacheManagerContext(CLUSTER_NAME, "member1")) {
			EmbeddedCacheManager manager = context.get();
			manager.defineConfiguration(cacheName, configuration);
			Cache<UUID, String> cache = manager.getCache(cacheName);
			cache.start();

			UUID id = UUID.randomUUID();
			String expected = "foo";

			cache.put(id, expected);

			CompletableFuture<Instant> future = new CompletableFuture<Instant>().orTimeout(10, TimeUnit.SECONDS);

			BooleanSupplier listener = new PassivationEventListener<>(future);
			cache.addListener(listener);

			Instant start = Instant.now();

			cache.evict(id);

			Instant stop = future.join();

			assertThat(listener.getAsBoolean()).isTrue();

			// Verify entry was found, but will reference the activated object
			assertThat(cache.get(id)).isEqualTo(expected).isNotSameAs(expected);

			assertThat(listener.getAsBoolean()).isFalse();

			cache.removeListener(listener);

			Duration duration = Duration.between(start, stop);
			Duration interval = Duration.ofNanos(duration.toNanos() / 100);

			System.out.println("Passivation time = " + duration);

			// Validate that Cache.get(...) does not fail to find entry while entry is passivating
			while (!duration.isNegative()) {
				cache.evict(id);

				Thread.sleep(duration.toMillis());

				assertThat(cache.get(id)).isEqualTo(expected);

				duration = duration.minus(interval);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@Listener
	private static class PassivationEventListener<K, V> implements BooleanSupplier {
		private final CompletableFuture<Instant> recorder;
		private final AtomicBoolean passive = new AtomicBoolean(false);

		PassivationEventListener(CompletableFuture<Instant> recorder) {
			this.recorder = recorder;
		}

		@Override
		public boolean getAsBoolean() {
			return this.passive.get();
		}

		@CacheEntryPassivated
		public void cacheEntriesPassivated(CacheEntryPassivatedEvent<K, V> event) {
			System.out.println(String.format("%s-passivate %s", event.isPre() ? "Pre" : "Post", event.getKey()));
			if (event.isPre()) {
				this.passive.set(true);
			} else {
				this.recorder.complete(Instant.now());
			}
		}

		@CacheEntryActivated
		public void cacheEntriesActivated(CacheEntryActivatedEvent<K, V> event) {
			System.out.println(String.format("%s-activate %s", event.isPre() ? "Pre" : "Post", event.getKey()));
			if (!event.isPre()) {
				this.passive.set(false);
				this.recorder.complete(Instant.now());
			}
		}
	}
}
