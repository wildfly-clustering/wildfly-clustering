/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.scheduler;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A task scheduler.
 * @param <I> the scheduled entry identifier type
 * @param <V> the scheduled entry value type
 * @author Paul Ferraro
 */
public interface Scheduler<I, V> extends AutoCloseable {
	/**
	 * Schedules a task for the object with the specified identifier, using the specified metaData
	 * @param id the scheduled entry identifier
	 * @param value the scheduled entry value
	 */
	void schedule(I id, V value);

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
	 * Returns scheduler wrapper of this scheduler.
	 * @param <T> the mapped type
	 * @param mapper a mapping function
	 * @return scheduler wrapper of this scheduler.
	 */
	default <T> Scheduler<I, T> map(Function<T, Optional<V>> mapper) {
		return new Scheduler<>() {
			@Override
			public void schedule(I id, T value) {
				Optional<V> mapped = mapper.apply(value);
				if (mapped.isPresent()) {
					Scheduler.this.schedule(id, mapped.get());
				}
			}

			@Override
			public void cancel(I id) {
				Scheduler.this.cancel(id);
			}

			@Override
			public boolean contains(I id) {
				return Scheduler.this.contains(id);
			}

			@Override
			public void close() {
				Scheduler.this.close();
			}
		};
	}

	/**
	 * Returns an inactive scheduler instance.
	 * @param <I> the scheduled entry identifier type
	 * @param <V> the scheduled entry value type
	 * @return an inactive scheduler instance.
	 */
	static <I, V> Scheduler<I, V> inactive() {
		return new InactiveScheduler<>();
	}

	/**
	 * Returns a scheduler that delegates to a scheduler reference.
	 * @param <I> the scheduled entry identifier type
	 * @param <V> the scheduled entry value type
	 * @param reference a scheduler reference
	 * @return a scheduler that delegates to a scheduler reference.
	 */
	static <I, V> Scheduler<I, V> fromReference(Supplier<? extends Scheduler<I, V>> reference) {
		return new ReferenceScheduler<>(reference);
	}

	/**
	 * An inactive scheduler implementation.
	 * @param <I> the scheduled object identifier type
	 * @param <V> the scheduled object type
	 */
	class InactiveScheduler<I, V> implements Scheduler<I, V> {
		/**
		 * Creates a new inactive scheduler.
		 */
		public InactiveScheduler() {
		}

		@Override
		public void schedule(I id, V value) {
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

	/**
	 * A scheduler decorator.
	 * @param <I> the scheduled object identifier type
	 * @param <V> the scheduled object type
	 */
	class ReferenceScheduler<I, V> implements Scheduler<I, V> {
		private final Supplier<? extends Scheduler<I, V>> reference;

		/**
		 * Creates a new referenced scheduler
		 * @param reference a reference to the decorated scheduler.
		 */
		public ReferenceScheduler(Supplier<? extends Scheduler<I, V>> reference) {
			this.reference = reference;
		}

		@Override
		public void schedule(I id, V metaData) {
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
