/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.server.scheduler.Scheduler;

/**
 * An abstract cache entry scheduler.
 * @param <I> the scheduled entry identifier type
 * @param <K> the cache entry key type
 * @param <V> the cache entry value type
 * @param <M> the scheduled entry metadata type
 * @author Paul Ferraro
 */
public abstract class AbstractCacheEntryScheduler<I, K extends Key<I>, V, M> extends Scheduler.ReferenceScheduler<I, M> implements CacheEntryScheduler<I, K, V, M> {

	protected AbstractCacheEntryScheduler(Scheduler<I, M> scheduler) {
		super(Supplier.of(scheduler));
	}
}
