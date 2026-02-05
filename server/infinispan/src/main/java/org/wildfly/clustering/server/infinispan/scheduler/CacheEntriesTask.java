/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.infinispan.Cache;
import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.CacheStreamFilter;
import org.wildfly.clustering.function.Consumer;

/**
 * Invokes a task against cache entries matching a filter.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
public class CacheEntriesTask<K, V> implements Consumer<CacheStreamFilter<Map.Entry<K, V>>> {
	private final Cache<K, V> cache;
	private final Predicate<Map.Entry<? super K, ? super V>> filter;
	private final Consumer<Map.Entry<K, V>> task;

	CacheEntriesTask(Cache<K, V> cache, Predicate<Map.Entry<? super K, ? super V>> filter, Consumer<Map.Entry<K, V>> task) {
		this.cache = cache;
		this.filter = filter;
		this.task = task;
	}

	/**
	 * Creates a task that schedules entries matching the specified filter.
	 * @param <I> the cache key identifier type
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @param <M> the group member type
	 * @param cache an embedded cache
	 * @param filter a cache entry filter
	 * @param scheduler the target scheduler
	 * @return a task that schedules entries matching the specified filter.
	 */
	public static <I, K extends Key<I>, V, M> Consumer<CacheStreamFilter<Map.Entry<K, V>>> schedule(Cache<K, V> cache, Predicate<Map.Entry<? super K, ? super V>> filter, CacheEntryScheduler<K, V> scheduler) {
		return new CacheEntriesTask<>(cache, filter, scheduler::scheduleEntry);
	}

	/**
	 * Creates a task that cancels entries matching the specified filter.
	 * @param <I> the cache key identifier type
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @param <M> the group member type
	 * @param cache an embedded cache
	 * @param filter a cache entry filter
	 * @param scheduler the target scheduler
	 * @return a task that cancels entries matching the specified filter.
	 */
	public static <I, K extends Key<I>, V, M> Consumer<CacheStreamFilter<Map.Entry<K, V>>> cancel(Cache<K, V> cache, Predicate<Map.Entry<? super K, ? super V>> filter, CacheEntryScheduler<K, V> scheduler) {
		org.wildfly.clustering.function.Consumer<K> cancel = scheduler::cancelKey;
		return new CacheEntriesTask<>(cache, filter, cancel.compose(Map.Entry::getKey));
	}

	@Override
	public void accept(CacheStreamFilter<Map.Entry<K, V>> filter) {
		// Iterate over filtered entries
		try (Stream<Map.Entry<K, V>> stream = filter.apply(this.cache.entrySet().stream()).filter(this.filter)) {
			Iterator<Map.Entry<K, V>> entries = stream.iterator();
			while (entries.hasNext()) {
				if (Thread.currentThread().isInterrupted()) break;
				this.task.accept(entries.next());
			}
		}
	}
}
