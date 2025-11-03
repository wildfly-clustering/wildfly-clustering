/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.List;
import java.util.function.Supplier;

/**
 * An enhanced runnable.
 * @author Paul Ferraro
 */
public interface Runner extends java.lang.Runnable {
	/** A runner that performs no action. */
	Runner EMPTY = () -> {};

	/**
	 * Returns a task that runs the specified task after running this task.
	 * @param task a task to run after this task
	 * @return a task that runs the specified task after running this task.
	 */
	default Runner andThen(java.lang.Runnable task) {
		return runAll(List.of(this, task));
	}

	/**
	 * Returns a new runnable that delegates to the specified handler in the event of an exception.
	 * @param handler a runtime exception handler
	 * @return a new runnable that delegates to the specified handler in the event of an exception.
	 */
	default Runner handle(Consumer<RuntimeException> handler) {
		return new Runner() {
			@Override
			public void run() {
				try {
					Runner.this.run();
				} catch (RuntimeException e) {
					handler.accept(e);
				}
			}
		};
	}

	/**
	 * Returns an empty task.
	 * @return an empty task.
	 */
	static Runner empty() {
		return EMPTY;
	}

	/**
	 * Returns a task that consumes a value from the specified supplier.
	 * @param <T> the consumed value
	 * @param consumer a consumer of the supplied value
	 * @param supplier a supplier of the consumed value
	 * @return a task that consumes a value from the specified supplier.
	 */
	static <T> Runner accept(java.util.function.Consumer<? super T> consumer, Supplier<? extends T> supplier) {
		return new Runner() {
			@Override
			public void run() {
				consumer.accept(supplier.get());
			}
		};
	}

	/**
	 * Returns a composite runner that runs the specified runners.
	 * @param runners zero or more runners
	 * @return a composite runner that runs the specified runners, logging any exceptions
	 */
	static Runner runAll(Iterable<? extends java.lang.Runnable> runners) {
		return new Runner() {
			@Override
			public void run() {
				runners.forEach(java.lang.Runnable::run);
			}
		};
	}
}
