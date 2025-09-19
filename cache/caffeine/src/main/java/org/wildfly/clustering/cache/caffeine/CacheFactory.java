/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.caffeine;

import java.util.OptionalLong;
import java.util.function.BiPredicate;

import org.wildfly.clustering.function.Function;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Weigher;

/**
 * @author Paul Ferraro
 * @param <K> the cache key type
 * @param <V> the cache value type
 */
public class CacheFactory<K, V> implements Function<CacheConfiguration<K, V>, Cache<K, V>> {
	private static final Expiry<Object, Object> NEVER = new Expiry<>() {
		@Override
		public long expireAfterCreate(Object key, Object value, long currentTime) {
			return Long.MAX_VALUE;
		}

		@Override
		public long expireAfterUpdate(Object key, Object value, long currentTime, long currentDuration) {
			return currentDuration;
		}

		@Override
		public long expireAfterRead(Object key, Object value, long currentTime, long currentDuration) {
			return currentDuration;
		}
	};

	@SuppressWarnings("unchecked")
	static <K, V> Expiry<K, V> never() {
		return (Expiry<K, V>) NEVER;
	}

	@Override
	public Cache<K, V> apply(CacheConfiguration<K, V> configuration) {
		@SuppressWarnings("unchecked")
		Caffeine<K, V> builder = (Caffeine<K, V>) Caffeine.newBuilder().executor(Runnable::run);
		Expiry<K, V> expiry = configuration.getExpiry();
		Weigher<K, V> weigher = configuration.getWeigher();
		if (expiry != NEVER) {
			// If a weigher was defined, ensure we exclude entries with no weight
			builder.expireAfter((weigher != Weigher.singletonWeigher()) ? new Expiry<K, V>() {
				private final BiPredicate<K, V> expirable = new BiPredicate<>() {
					@Override
					public boolean test(K key, V value) {
						return weigher.weigh(key, value) > 0;
					}
				};

				@Override
				public long expireAfterCreate(K key, V value, long currentTime) {
					return this.expirable.test(key, value) ? expiry.expireAfterCreate(key, value, currentTime) : Long.MAX_VALUE;
				}

				@Override
				public long expireAfterUpdate(K key, V value, long currentTime, long currentDuration) {
					return this.expirable.test(key, value) ? expiry.expireAfterUpdate(key, value, currentTime, currentDuration) : currentDuration;
				}

				@Override
				public long expireAfterRead(K key, V value, long currentTime, long currentDuration) {
					return this.expirable.test(key, value) ? expiry.expireAfterRead(key, value, currentTime, currentDuration) : currentDuration;
				}
			} : expiry).scheduler(configuration.getScheduler());
		}
		OptionalLong maxWeight = configuration.getMaxWeight();
		if (maxWeight.isPresent()) {
			// Avoid weight calculation if possible
			if (weigher == Weigher.singletonWeigher()) {
				builder.maximumSize(maxWeight.getAsLong());
			} else {
				builder.maximumWeight(maxWeight.getAsLong()).weigher(weigher);
			}
		}
		configuration.getEvictionListener().ifPresent(builder::evictionListener);
		configuration.getRemovalListener().ifPresent(builder::removalListener);
		return builder.build();
	}
}
