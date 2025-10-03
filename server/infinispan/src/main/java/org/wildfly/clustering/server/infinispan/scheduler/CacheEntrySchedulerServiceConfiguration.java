/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.server.scheduler.SchedulerService;

/**
 * Configuration of a cache entry scheduler.
 * @author Paul Ferraro
 * @param <I> the scheduled item identifier type
 * @param <K> the cache key type
 * @param <V> the cache value type
 * @param <M> the scheduled item metadata type
 */
public interface CacheEntrySchedulerServiceConfiguration<I, K extends Key<I>, V, M> {
	/**
	 * Returns the decorated scheduler service.
	 * @return the decorated scheduler service.
	 */
	SchedulerService<I, M> getSchedulerService();

	/**
	 * Returns the task to invoke on {@link SchedulerService#start()}.
	 * @return the task to invoke on {@link SchedulerService#start()}.
	 */
	default java.util.function.Consumer<CacheEntryScheduler<K, V>> getStartTask() {
		return Consumer.empty();
	}

	/**
	 * Returns the task to invoke on {@link SchedulerService#stop()}.
	 * @return the task to invoke on {@link SchedulerService#stop()}.
	 */
	default java.util.function.Consumer<CacheEntryScheduler<K, V>> getStopTask() {
		return Consumer.empty();
	}

	/**
	 * Returns the locator function.
	 * @return the locator function.
	 */
	Function<I, V> getLocator();

	/**
	 * Returns the meta data function.
	 * @return the meta data function.
	 */
	BiFunction<I, V, M> getMetaData();
}
