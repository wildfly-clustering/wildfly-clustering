/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.function.Supplier;

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
	 * Cancels any previous scheduled tasks for entries which are no longer local to the current member
	 * @param locality the cache locality
	 */
	void cancel(Locality locality);

	/**
	 * Returns an inactive scheduler instance.
	 * @param <I> the scheduled entry identifier type
	 * @param <M> the scheduled entry metadata type
	 * @return an inactive scheduler instance.
	 */
	static <I, M> CacheEntryScheduler<I, M> inactive() {
		return new InactiveCacheEntryScheduler<>();
	}

	/**
	 * Returns a scheduler that delegates to a scheduler reference.
	 * @param reference a scheduler reference
	 * @param <I> the scheduled entry identifier type
	 * @param <M> the scheduled entry metadata type
	 * @return a scheduler that delegates to a scheduler reference.
	 */
	static <I, M> CacheEntryScheduler<I, M> reference(Supplier<? extends CacheEntryScheduler<I, M>> reference) {
		return new ReferenceCacheEntryScheduler<>(reference);
	}

	class InactiveCacheEntryScheduler<I, M> extends Scheduler.InactiveScheduler<I, M> implements CacheEntryScheduler<I, M> {
		@Override
		public void schedule(I id) {
		}

		@Override
		public void cancel(Locality locality) {
		}
	}

	class ReferenceCacheEntryScheduler<I, M> extends Scheduler.ReferenceScheduler<I, M> implements CacheEntryScheduler<I, M> {
		private final Supplier<? extends CacheEntryScheduler<I, M>> reference;

		ReferenceCacheEntryScheduler(Supplier<? extends CacheEntryScheduler<I, M>> reference) {
			super(reference);
			this.reference = reference;
		}

		@Override
		public void schedule(I id) {
			this.reference.get().schedule(id);
		}

		@Override
		public void cancel(Locality locality) {
			this.reference.get().cancel(locality);
		}
	}
}
