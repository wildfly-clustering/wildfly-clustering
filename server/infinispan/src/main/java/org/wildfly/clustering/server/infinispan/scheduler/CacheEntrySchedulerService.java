/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.server.scheduler.DecoratedSchedulerService;

/**
 * A cache entry scheduler facade for a scheduler.
 * @author Paul Ferraro
 * @param <I> the scheduled item identifier type
 * @param <K> the cache key type
 * @param <V> the cache value type
 * @param <M> the scheduled item metadata type
 */
public class CacheEntrySchedulerService<I, K extends Key<I>, V, M> extends DecoratedSchedulerService<I, M> implements CacheEntryScheduler<K, V>, SchedulerService<I, M> {

	private final Function<I, V> locator;
	private final BiFunction<I, V, M> metaData;

	/**
	 * Creates a cache entry scheduler for the specified scheduler service.
	 * @param scheduler a scheduler service
	 * @param locator a cache entry locator
	 * @param metaData a function returning the scheduled item metadata for a given cache entry
	 */
	public CacheEntrySchedulerService(org.wildfly.clustering.server.scheduler.SchedulerService<I, M> scheduler, Function<I, V> locator, BiFunction<I, V, M> metaData) {
		super(scheduler);
		this.locator = locator;
		this.metaData = metaData;
	}

	@Override
	public void schedule(I id) {
		V value = this.locator.apply(id);
		if (value != null) {
			this.schedule(id, this.metaData.apply(id, value));
		}
	}

	@Override
	public void scheduleKey(K key) {
		this.schedule(key.getId());
	}

	@Override
	public void scheduleEntry(Map.Entry<K, V> entry) {
		I id = entry.getKey().getId();
		this.schedule(id, this.metaData.apply(id, entry.getValue()));
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
