/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.caffeine;

import java.time.Duration;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;

import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.Scheduler;
import com.github.benmanes.caffeine.cache.Weigher;

/**
 * Encapsulates the configuration of a Caffeine cache.
 * @author Paul Ferraro
 * @param <K> the cache key type
 * @param <V> the cache value type
 */
public interface CacheConfiguration<K, V> {

	/**
	 * When present, defines the maximum weight for entries within this cache.
	 * @return an optional size-based eviction threshold
	 */
	OptionalLong getMaxWeight();

	/**
	 * Returns the expiry logic of this cache.
	 * @return the expiry logic of this cache.
	 */
	Expiry<K, V> getExpiry();

	/**
	 * Returns a weigher of cache entries.
	 * @return a weigher of cache entries.
	 */
	Weigher<K, V> getWeigher();

	/**
	 * When present, defines a listener to be notified on entry eviction.
	 * @return an optional listener to be notified on entry eviction.
	 */
	Optional<RemovalListener<K, V>> getEvictionListener();

	/**
	 * When present, defines a listener to be notified on entry removal.
	 * @return an optional listener to be notified on entry removal.
	 */
	Optional<RemovalListener<K, V>> getRemovalListener();

	/**
	 * Returns a scheduler of asynchronous tasks.
	 * @return a scheduler of asynchronous tasks.
	 */
	Scheduler getScheduler();

	/**
	 * A builder of a Caffeine cache configuration.
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 */
	interface Builder<K, V> {
		/**
		 * Defines the maximum weight of entries in the cache
		 * @param weight the maximum weight.
		 * @return a reference to this builder
		 */
		Builder<K, V> withMaxWeight(long weight);

		/**
		 * Defines a predicate indicating whether a given entry is evictable.
		 * @param evictable a predicate indicating whether a given entry is evictable.
		 * @return a reference to this builder
		 */
		default Builder<K, V> evictableWhen(Predicate<K> evictable) {
			return this.evictableWhen((evictable != org.wildfly.clustering.function.Predicate.of(true)) ? org.wildfly.clustering.function.BiPredicate.former(evictable) : org.wildfly.clustering.function.BiPredicate.of(true));
		}

		/**
		 * Defines a predicate indicating whether a given entry is evictable.
		 * @param evictable a predicate indicating whether a given entry is evictable.
		 * @return a reference to this builder
		 */
		default Builder<K, V> evictableWhen(BiPredicate<K, V> evictable) {
			return this.withWeigher((evictable != org.wildfly.clustering.function.BiPredicate.of(true)) ? new Weigher<K, V>() {
				@Override
				public int weigh(K key, V value) {
					return evictable.test(key, value) ? 1 : 0;
				}
			} : Weigher.singletonWeigher());
		}

		/**
		 * Defines a function used to determine the weight of a given cache entry.
		 * @param weight a function used to determine the weight of a given cache entry.
		 * @return a reference to this builder
		 */
		default Builder<K, V> withWeight(ToIntBiFunction<K, V> weight) {
			Weigher<K, V> weigher = weight::applyAsInt;
			return this.withWeigher(weigher);
		}

		/**
		 * Defines a weigher used to determine the weight a given cache entry.
		 * @param weigher a weigher used to determine the weight a given cache entry.
		 * @return a reference to this builder
		 */
		Builder<K, V> withWeigher(Weigher<K, V> weigher);

		/**
		 * Defines a duration of time after which idle entries should be auto-evicted.
		 * @param idleTimeout a duration of time after which idle entries should be auto-evicted.
		 * @return a reference to this builder
		 */
		default Builder<K, V> evictAfter(Duration idleTimeout) {
			return (idleTimeout != null) ? this.evictAfter(org.wildfly.clustering.function.Function.of(idleTimeout)) : this.withExpiry(CacheFactory.never());
		}

		/**
		 * Defines a function returning the duration of time after which a given idle entry should be auto-evicted.
		 * @param idleTimeout a function returning the duration of time after which a given idle entry should be auto-evicted.
		 * @return a reference to this builder
		 */
		default Builder<K, V> evictAfter(Function<V, Duration> idleTimeout) {
			return this.evictAfter(org.wildfly.clustering.function.BiFunction.latter(idleTimeout));
		}

		/**
		 * Defines a function returning the duration of time after which a given idle entry should be auto-evicted.
		 * @param idleTimeout a function returning the duration of time after which a given idle entry should be auto-evicted.
		 * @return a reference to this builder
		 */
		default Builder<K, V> evictAfter(BiFunction<K, V, Duration> idleTimeout) {
			return this.withExpiry(new Expiry<K, V>() {
				@Override
				public long expireAfterCreate(K key, V value, long currentTime) {
					return idleTimeout.apply(key, value).toNanos();
				}

				@Override
				public long expireAfterUpdate(K key, V value, long currentTime, long currentDuration) {
					return idleTimeout.apply(key, value).toNanos();
				}

				@Override
				public long expireAfterRead(K key, V value, long currentTime, long currentDuration) {
					return idleTimeout.apply(key, value).toNanos();
				}
			});
		}

		/**
		 * Defines the logic used to determine the duration of time after which a given idle entry should be auto-evicted.
		 * @param expiry the logic used to determine the duration of time after which a given idle entry should be auto-evicted.
		 * @return a reference to this builder
		 */
		Builder<K, V> withExpiry(Expiry<K, V> expiry);

		/**
		 * Defines a consumer to be notified on entry eviction.
		 * @param handler an eviction handler
		 * @return a reference to this builder
		 */
		default Builder<K, V> whenEvicted(BiConsumer<K, V> handler) {
			return this.whenEvicted(new RemovalListener<>() {
				@Override
				public void onRemoval(K key, V value, RemovalCause cause) {
					if ((cause == RemovalCause.EXPIRED) || (cause == RemovalCause.SIZE)) {
						handler.accept(key, value);
					}
				}
			});
		}

		/**
		 * Defines a listener to be notified on entry eviction.
		 * @param listener an eviction listener
		 * @return a reference to this builder
		 */
		Builder<K, V> whenEvicted(RemovalListener<K, V> listener);

		/**
		 * Defines a consumer to be notified on entry removal.
		 * @param handler a removal handler
		 * @return a reference to this builder
		 */
		default Builder<K, V> whenRemoved(BiConsumer<K, V> handler) {
			return this.whenRemoved(new RemovalListener<>() {
				@Override
				public void onRemoval(K key, V value, RemovalCause cause) {
					if ((cause == RemovalCause.EXPIRED) || (cause == RemovalCause.SIZE)) {
						handler.accept(key, value);
					}
				}
			});
		}

		/**
		 * Defines a listener to be notified on entry removal.
		 * @param listener a removal listener
		 * @return a reference to this builder
		 */
		Builder<K, V> whenRemoved(RemovalListener<K, V> listener);

		/**
		 * Defines an executor for use with time-based eviction.
		 * @param executor an executor for use with time-based eviction.
		 * @return a reference to this builder
		 */
		default Builder<K, V> withExecutor(ScheduledExecutorService executor) {
			return this.withScheduler((executor != null) ? Scheduler.forScheduledExecutorService(executor) : Scheduler.systemScheduler());
		}

		/**
		 * Defines a scheduler for use with time-based eviction.
		 * @param scheduler a scheduler for use with time-based eviction.
		 * @return a reference to this builder
		 */
		Builder<K, V> withScheduler(Scheduler scheduler);

		/**
		 * Builds a cache configuration.
		 * @return a cache configuration.
		 */
		CacheConfiguration<K, V> build();
	}

	/**
	 * Creates a builder of cache configuration.
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @return a builder of cache configuration.
	 */
	static <K, V> Builder<K, V> builder() {
		return new Builder<>() {
			private OptionalLong maxWeight = OptionalLong.empty();
			private Expiry<K, V> expiry = CacheFactory.never();
			private Weigher<K, V> weigher = Weigher.singletonWeigher();
			private Optional<RemovalListener<K, V>> evictionListener = Optional.empty();
			private Optional<RemovalListener<K, V>> removalListener = Optional.empty();
			private Scheduler scheduler = Scheduler.systemScheduler();

			@Override
			public Builder<K, V> withMaxWeight(long weight) {
				this.maxWeight = OptionalLong.of(weight);
				return this;
			}

			@Override
			public Builder<K, V> withWeigher(Weigher<K, V> weigher) {
				this.weigher = weigher;
				return this;
			}

			@Override
			public Builder<K, V> withExpiry(Expiry<K, V> expiry) {
				this.expiry = expiry;
				return this;
			}

			@Override
			public Builder<K, V> whenEvicted(RemovalListener<K, V> listener) {
				this.evictionListener = Optional.of(listener);
				return this;
			}

			@Override
			public Builder<K, V> whenRemoved(RemovalListener<K, V> listener) {
				this.removalListener = Optional.of(listener);
				return this;
			}

			@Override
			public Builder<K, V> withScheduler(Scheduler scheduler) {
				this.scheduler = scheduler;
				return this;
			}

			@Override
			public CacheConfiguration<K, V> build() {
				OptionalLong maxWeight = this.maxWeight;
				Expiry<K, V> expiry = this.expiry;
				Weigher<K, V> weigher = this.weigher;
				Optional<RemovalListener<K, V>> evictionListener = this.evictionListener;
				Optional<RemovalListener<K, V>> removalListener = this.removalListener;
				Scheduler scheduler = this.scheduler;
				return new CacheConfiguration<>() {
					@Override
					public OptionalLong getMaxWeight() {
						return maxWeight;
					}

					@Override
					public Expiry<K, V> getExpiry() {
						return expiry;
					}

					@Override
					public Weigher<K, V> getWeigher() {
						return weigher;
					}

					@Override
					public Optional<RemovalListener<K, V>> getEvictionListener() {
						return evictionListener;
					}

					@Override
					public Optional<RemovalListener<K, V>> getRemovalListener() {
						return removalListener;
					}

					@Override
					public Scheduler getScheduler() {
						return scheduler;
					}
				};
			}
		};
	}
}
