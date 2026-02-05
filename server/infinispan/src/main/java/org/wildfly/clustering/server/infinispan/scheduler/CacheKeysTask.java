/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.infinispan.Cache;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.CacheStreamFilter;
import org.wildfly.clustering.function.Consumer;

/**
 * Invokes a task against cache entries matching a filter.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
public class CacheKeysTask<K, V> implements Consumer<CacheStreamFilter<K>> {
	private final Cache<K, V> cache;
	private final Predicate<? super K> filter;
	private final Consumer<K> task;

	CacheKeysTask(Cache<K, V> cache, Predicate<? super K> filter, Consumer<K> task) {
		this.cache = cache;
		this.filter = filter;
		this.task = task;
	}

	/**
	 * Creates a schedule task for keys matching the specified filter.
	 * @param <K> the cache entry key type
	 * @param <V> the cache entry value type
	 * @param cache an embedded cache
	 * @param filter a cache key filter
	 * @param scheduler a scheduler
	 * @return a schedule task for keys matching the specified filter.
	 */
	public static <K, V> Consumer<CacheStreamFilter<K>> cancel(Cache<K, V> cache, Predicate<? super K> filter, CacheEntryScheduler<K, V> scheduler) {
		return new CacheKeysTask<>(cache, filter, scheduler::cancelKey);
	}

	@Override
	public void accept(CacheStreamFilter<K> filter) {
		// Iterate over filtered keys
		try (Stream<K> stream = filter.apply(this.cache.keySet().stream()).filter(this.filter)) {
			Iterator<K> keys = stream.iterator();
			while (keys.hasNext()) {
				if (Thread.currentThread().isInterrupted()) break;
				this.task.accept(keys.next());
			}
		}
	}
}
