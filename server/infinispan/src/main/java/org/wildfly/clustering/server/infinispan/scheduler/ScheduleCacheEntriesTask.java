/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.infinispan.Cache;
import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.CacheStreamFilter;

/**
 * A task which schedules newly owned entries.
 * @author Paul Ferraro
 * @param <I> identifier type
 * @param <M> the expiration metadata type
 * @param <K> cache key type
 * @param <V> cache value type
 */
public class ScheduleCacheEntriesTask<I, M, K extends Key<I>, V> implements Consumer<CacheStreamFilter<Map.Entry<K, V>>> {
	private final Cache<K, V> cache;
	private final Predicate<Map.Entry<? super K, ? super V>> filter;
	private final CacheEntryScheduler<I, M> scheduler;

	public ScheduleCacheEntriesTask(Cache<K, V> cache, Predicate<Map.Entry<? super K, ? super V>> filter, CacheEntryScheduler<I, M> scheduler) {
		this.cache = cache;
		this.filter = filter;
		this.scheduler = scheduler;
	}

	@Override
	public void accept(CacheStreamFilter<Map.Entry<K, V>> filter) {
		// Iterate over local entries, including any cache stores to include entries that may be passivated/invalidated
		try (Stream<Map.Entry<K, V>> stream = filter.apply(this.cache.entrySet().stream()).filter(this.filter)) {
			Iterator<Map.Entry<K, V>> entries = stream.iterator();
			while (entries.hasNext()) {
				if (Thread.currentThread().isInterrupted()) break;
				this.scheduler.schedule(entries.next().getKey().getId());
			}
		}
	}
}
