/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.function.Supplier;

/**
 * Scheduler that does not require predetermined entry meta data.
 * @author Paul Ferraro
 * @param <I> the scheduled entry identifier type
 * @param <M> the scheduled entry metadata type
 */
public interface Scheduler<I, M> extends org.wildfly.clustering.server.scheduler.Scheduler<I, M> {

	/**
	 * Schedules an entry for the specified identifier.
	 * @param id a scheduled entry identifier
	 */
	void schedule(I id);

	class InactiveScheduler<I, M> extends org.wildfly.clustering.server.scheduler.Scheduler.InactiveScheduler<I, M> implements Scheduler<I, M> {
		@Override
		public void schedule(I id) {
		}
	}

	class ReferenceScheduler<I, M> extends org.wildfly.clustering.server.scheduler.Scheduler.ReferenceScheduler<I, M> implements Scheduler<I, M> {
		private final Supplier<? extends Scheduler<I, M>> reference;

		ReferenceScheduler(Supplier<? extends Scheduler<I, M>> reference) {
			super(reference);
			this.reference = reference;
		}

		@Override
		public void schedule(I id) {
			this.reference.get().schedule(id);
		}
	}
}
