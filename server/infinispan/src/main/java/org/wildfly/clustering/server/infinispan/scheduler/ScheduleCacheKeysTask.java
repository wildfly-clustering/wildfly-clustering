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
 * A task which schedules newly owned keys.
 * @author Paul Ferraro
 * @param <I> identifier type
 * @param <K> cache key type
 */
public class ScheduleCacheKeysTask<I, K extends Key<I>> implements Consumer<CacheStreamFilter<K>> {
	private final Cache<K, ?> cache;
	private final Predicate<? super K> filter;
	private final Consumer<I> scheduleTask;

	public ScheduleCacheKeysTask(Cache<K, ?> cache, Predicate<? super K> filter, CacheEntryScheduler<I, ?> scheduler) {
		this(cache, filter, scheduler::schedule);
	}

	public ScheduleCacheKeysTask(Cache<K, ?> cache, Predicate<? super K> filter, Consumer<I> scheduleTask) {
		this.cache = cache;
		this.filter = filter;
		this.scheduleTask = scheduleTask;
	}

	@Override
	public void accept(CacheStreamFilter<K> filter) {
		// Iterate over keys for newly owned segments
		try (Stream<K> stream = filter.apply(this.cache.keySet().stream()).filter(this.filter)) {
			Iterator<K> keys = stream.iterator();
			while (keys.hasNext()) {
				if (Thread.currentThread().isInterrupted()) break;
				this.scheduleTask.accept(keys.next().getId());
			}
		}
	}
}
