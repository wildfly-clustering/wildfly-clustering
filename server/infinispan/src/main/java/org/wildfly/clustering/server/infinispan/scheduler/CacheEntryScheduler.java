/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import org.wildfly.clustering.cache.infinispan.embedded.distribution.Locality;
import org.wildfly.clustering.server.scheduler.Scheduler;

/**
 * A task scheduler.
 * @param <I> the identifier type of scheduled entries
 * @param <M> the meta data type
 * @author Paul Ferraro
 */
public interface CacheEntryScheduler<I, M> extends Scheduler<I, M> {
	/**
	 * Schedules a cache entry with the specified identifier.
	 * This method will generally delegate to {@link #schedule(Object, Object)} after performing a cache lookup.
	 * @param id the identifier of the object to be scheduled
	 */
	void schedule(I id);

	/**
	 * Cancels any previous scheduled tasks for entries which are no longer local to the current node
	 * @param locality the cache locality
	 */
	void cancel(Locality locality);
}
