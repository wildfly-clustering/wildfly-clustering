/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.Map;
import java.util.function.BiFunction;

import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.server.scheduler.DecoratedSchedulerService;
import org.wildfly.clustering.server.scheduler.SchedulerService;

/**
 * A cache entry scheduler facade for a scheduler.
 * @author Paul Ferraro
 * @param <I> the scheduled item identifier type
 * @param <K> the cache key type
 * @param <V> the cache value type
 * @param <T> the scheduled item metadata type
 */
public class CacheEntrySchedulerService<I, K extends Key<I>, V, T> extends DecoratedSchedulerService<I, T> implements CacheEntryScheduler<K, V> {

	private final BiFunction<I, V, T> mapper;

	/**
	 * Creates a cache entry scheduler from the specified scheduler and mapper.
	 * @param scheduler the decorated scheduler
	 * @param mapper the function mapping a cache entry to the scheduled value.
	 */
	public CacheEntrySchedulerService(SchedulerService<I, T> scheduler, BiFunction<I, V, T> mapper) {
		super(scheduler);
		this.mapper = mapper;
	}

	@Override
	public void scheduleEntry(Map.Entry<K, V> entry) {
		I id = entry.getKey().getId();
		this.schedule(id, this.mapper.apply(id, entry.getValue()));
	}

	@Override
	public void cancelKey(K key) {
		this.cancel(key.getId());
	}

	@Override
	public boolean containsKey(K key) {
		return this.contains(key.getId());
	}
}
