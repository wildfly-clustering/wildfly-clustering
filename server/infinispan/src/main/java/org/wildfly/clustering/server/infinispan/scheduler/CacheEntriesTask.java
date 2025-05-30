/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.infinispan.Cache;
import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.CacheStreamFilter;

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

	public static <I, K extends Key<I>, V, M> CacheEntriesTask<K, V> schedule(Cache<K, V> cache, Predicate<Map.Entry<? super K, ? super V>> filter, CacheEntryScheduler<I, K, V, M> scheduler) {
		return new CacheEntriesTask<>(cache, filter, scheduler::schedule);
	}

	public static <I, K extends Key<I>, V, M> CacheEntriesTask<K, V> cancel(Cache<K, V> cache, Predicate<Map.Entry<? super K, ? super V>> filter, CacheEntryScheduler<I, K, V, M> scheduler) {
		org.wildfly.clustering.function.Consumer<I> cancel = scheduler::cancel;
		Function<Map.Entry<K, V>, K> key = Map.Entry::getKey;
		return new CacheEntriesTask<>(cache, filter, cancel.compose(key.andThen(Key::getId)));
	}

	public CacheEntriesTask(Cache<K, V> cache, Predicate<Map.Entry<? super K, ? super V>> filter, Consumer<Map.Entry<K, V>> task) {
		this.cache = cache;
		this.filter = filter;
		this.task = task;
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
