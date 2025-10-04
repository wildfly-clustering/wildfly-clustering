/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
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
	private final Consumer<CacheEntryScheduler<K, V>> startTask;
	private final Consumer<CacheEntryScheduler<K, V>> stopTask;
	private final Function<I, V> locator;
	private final BiFunction<I, V, M> metaData;

	/**
	 * Creates a cache entry scheduler from the specified configuration.
	 * @param configuration the scheduler configuration
	 */
	public CacheEntrySchedulerService(CacheEntrySchedulerServiceConfiguration<I, K, V, M> configuration) {
		super(configuration.getSchedulerService());
		this.startTask = configuration.getStartTask();
		this.stopTask = configuration.getStopTask();
		this.locator = configuration.getLocator();
		this.metaData = configuration.getMetaData();
	}

	@Override
	public void start() {
		super.start();
		this.startTask.accept(this);
	}

	@Override
	public void stop() {
		this.stopTask.accept(this);
		super.stop();
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
