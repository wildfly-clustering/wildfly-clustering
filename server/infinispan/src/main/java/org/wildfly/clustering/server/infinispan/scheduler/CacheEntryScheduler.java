/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.Map;
import java.util.function.Supplier;

import org.wildfly.clustering.cache.Key;

/**
 * A task scheduler.
 * @param <I> the scheduled entry identifier type
 * @param <K> the cache entry key type
 * @param <V> the cache entry value type
 * @param <M> the scheduled entry metadata type
 * @author Paul Ferraro
 */
public interface CacheEntryScheduler<I, K extends Key<I>, V, M> extends Scheduler<I, M> {

	/**
	 * Schedules a cache entry.
	 * @param entry a cache entry
	 */
	void schedule(Map.Entry<K, V> entry);

	/**
	 * Returns an inactive scheduler instance.
	 * @param <I> the scheduled object identifier type
	 * @param <K> the cache entry key type
	 * @param <V> the cache entry value type
	 * @param <M> the scheduled object metadata type
	 * @return an inactive scheduler instance.
	 */
	static <I, K extends Key<I>, V, M> CacheEntryScheduler<I, K, V, M> inactive() {
		return new InactiveCacheEntryScheduler<>();
	}

	/**
	 * Returns a scheduler that delegates to a scheduler reference.
	 * @param reference a scheduler reference
	 * @param <I> the scheduled object identifier type
	 * @param <K> the cache entry key type
	 * @param <V> the cache entry value type
	 * @param <M> the scheduled object metadata type
	 * @return a scheduler that delegates to a scheduler reference.
	 */
	static <I, K extends Key<I>, V, M> CacheEntryScheduler<I, K, V, M> fromReference(Supplier<? extends CacheEntryScheduler<I, K, V, M>> reference) {
		return new ReferenceCacheEntryScheduler<>(reference);
	}

	/**
	 * An inactive cache entry scheduler.
	 * @param <I> the scheduled item identifier type
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @param <M> the scheduled item metadata type
	 */
	class InactiveCacheEntryScheduler<I, K extends Key<I>, V, M> extends Scheduler.InactiveScheduler<I, M> implements CacheEntryScheduler<I, K, V, M> {
		/**
		 * Creates an inactive cache entry scheduler.
		 */
		InactiveCacheEntryScheduler() {
		}

		@Override
		public void schedule(Map.Entry<K, V> entry) {
		}
	}

	/**
	 * A cache entry scheduler decorator.
	 * @param <I> the scheduled item identifier type
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @param <M> the scheduled item metadata type
	 */
	class ReferenceCacheEntryScheduler<I, K extends Key<I>, V, M> extends Scheduler.ReferenceScheduler<I, M> implements CacheEntryScheduler<I, K, V, M> {
		private final Supplier<? extends CacheEntryScheduler<I, K, V, M>> reference;

		ReferenceCacheEntryScheduler(Supplier<? extends CacheEntryScheduler<I, K, V, M>> reference) {
			super(reference);
			this.reference = reference;
		}

		@Override
		public void schedule(Map.Entry<K, V> entry) {
			this.reference.get().schedule(entry);
		}
	}
}
