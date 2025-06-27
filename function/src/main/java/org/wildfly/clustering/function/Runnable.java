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
public interface Runnable extends java.lang.Runnable {
	Runnable EMPTY = new Runnable() {
		@Override
		public void run() {
			// Do nothing
		}
	};

	/**
	 * Returns a task that runs the specified task after running this task.
	 * @param task a task to run after this task
	 * @return a task that runs the specified task after running this task.
	 */
	default Runnable andThen(java.lang.Runnable task) {
		return runAll(List.of(this, task));
	}

	/**
	 * Returns an empty task.
	 * @return an empty task.
	 */
	static Runnable empty() {
		return EMPTY;
	}

	/**
	 * Adds runtime exception handling to a {@link java.lang.Runnable}.
	 * @param runner a runnable
	 * @param exceptionHandler a runtime exception handler
	 * @return a runnable that handles runtime exceptions thrown by the specified runnable.
	 */
	static Runnable run(java.lang.Runnable runner, Consumer<RuntimeException> exceptionHandler) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					runner.run();
				} catch (RuntimeException e) {
					exceptionHandler.accept(e);
				}
			}
		};
	}

	/**
	 * Returns a task that consumes a value from the specified supplier.
	 * @param <T> the consumed value
	 * @param consumer a consumer of the supplied value
	 * @param supplier a supplier of the consumed value
	 * @return a task that consumes a value from the specified supplier.
	 */
	static <T> Runnable accept(java.util.function.Consumer<? super T> consumer, Supplier<? extends T> supplier) {
		return new Runnable() {
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
	static Runnable runAll(Iterable<? extends java.lang.Runnable> runners) {
		return new Runnable() {
			@Override
			public void run() {
				for (java.lang.Runnable runner : runners) {
					try {
						runner.run();
					} catch (RuntimeException e) {
						Consumer.warning().accept(e);
					}
				}
			}
		};
	}
}
