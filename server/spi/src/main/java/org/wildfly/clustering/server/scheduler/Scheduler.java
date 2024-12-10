/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.scheduler;

import java.util.function.Supplier;

/**
 * A task scheduler.
 * @param <I> the scheduled entry identifier type
 * @param <M> the scheduled entry metadata type
 * @author Paul Ferraro
 */
public interface Scheduler<I, M> extends AutoCloseable {
	/**
	 * Schedules a task for the object with the specified identifier, using the specified metaData
	 * @param id an object identifier
	 * @param metaData the object meta-data
	 */
	void schedule(I id, M metaData);

	/**
	 * Cancels a previously scheduled task for the object with the specified identifier.
	 * @param id an object identifier
	 */
	void cancel(I id);

	/**
	 * Indicates whether the entry with the specified identifier is scheduled.
	 * @param id an object identifier
	 * @return true, if the specified entry is scheduled, false otherwise.
	 */
	boolean contains(I id);

	@Override
	void close();

	/**
	 * Returns an inactive scheduler instance.
	 * @param <I> the scheduled entry identifier type
	 * @param <M> the scheduled entry metadata type
	 * @return an inactive scheduler instance.
	 */
	static <I, M> Scheduler<I, M> inactive() {
		return new InactiveScheduler<>();
	}

	/**
	 * Returns a scheduler that delegates to a scheduler reference.
	 * @param reference a scheduler reference
	 * @param <I> the scheduled entry identifier type
	 * @param <M> the scheduled entry metadata type
	 * @return a scheduler that delegates to a scheduler reference.
	 */
	static <I, M> Scheduler<I, M> fromReference(Supplier<? extends Scheduler<I, M>> reference) {
		return new ReferenceScheduler<>(reference);
	}

	class InactiveScheduler<I, M> implements Scheduler<I, M> {
		protected InactiveScheduler() {
		}

		@Override
		public void schedule(I id, M metaData) {
		}

		@Override
		public void cancel(I id) {
		}

		@Override
		public boolean contains(I id) {
			return false;
		}

		@Override
		public void close() {
		}
	}

	class ReferenceScheduler<I, M> implements Scheduler<I, M> {
		private final Supplier<? extends Scheduler<I, M>> reference;

		protected ReferenceScheduler(Supplier<? extends Scheduler<I, M>> reference) {
			this.reference = reference;
		}

		@Override
		public void schedule(I id, M metaData) {
			this.reference.get().schedule(id, metaData);
		}

		@Override
		public void cancel(I id) {
			this.reference.get().cancel(id);
		}

		@Override
		public boolean contains(I id) {
			return this.reference.get().contains(id);
		}

		@Override
		public void close() {
			this.reference.get().close();
		}
	}
}
