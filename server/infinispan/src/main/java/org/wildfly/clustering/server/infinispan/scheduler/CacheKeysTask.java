/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.Iterator;
import java.util.function.Consumer;
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
public class CacheKeysTask<K, V> implements Consumer<CacheStreamFilter<K>> {
	private final Cache<K, V> cache;
	private final Predicate<? super K> filter;
	private final Consumer<K> task;

	public static <I, K extends Key<I>, V, M> CacheKeysTask<K, V> schedule(Cache<K, V> cache, Predicate<? super K> filter, Scheduler<I, M> scheduler) {
		org.wildfly.clustering.function.Consumer<I> schedule = scheduler::schedule;
		return new CacheKeysTask<>(cache, filter, schedule.compose(Key::getId));
	}

	public static <I, K extends Key<I>, V, M> CacheKeysTask<K, V> cancel(Cache<K, V> cache, Predicate<? super K> filter, Scheduler<I, M> scheduler) {
		org.wildfly.clustering.function.Consumer<I> cancel = scheduler::cancel;
		return new CacheKeysTask<>(cache, filter, cancel.compose(Key::getId));
	}

	public CacheKeysTask(Cache<K, V> cache, Predicate<? super K> filter, Consumer<K> task) {
		this.cache = cache;
		this.filter = filter;
		this.task = task;
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
