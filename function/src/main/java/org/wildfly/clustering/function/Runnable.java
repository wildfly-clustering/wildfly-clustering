/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.List;
import java.util.function.Consumer;
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
		return of(List.of(this, task));
	}

	/**
	 * Returns an empty task.
	 * @return an empty task.
	 */
	static Runnable empty() {
		return EMPTY;
	}

	/**
	 * Returns a task that consumes a value from the specified supplier.
	 * @param <T> the consumed value
	 * @param consumer a consumer of the supplied value
	 * @param supplier a supplier of the consumed value
	 * @return a task that consumes a value from the specified supplier.
	 */
	static <T> Runnable of(Consumer<? super T> consumer, Supplier<? extends T> supplier) {
		return new Runnable() {
			@Override
			public void run() {
				consumer.accept(supplier.get());
			}
		};
	}

	/**
	 * Returns a composite task that runs the specified task.
	 * @param tasks zero or more tasks
	 * @return a composite task that runs the specified task.
	 */
	static Runnable of(Iterable<? extends java.lang.Runnable> tasks) {
		return new Runnable() {
			@Override
			public void run() {
				for (java.lang.Runnable task : tasks) {
					task.run();
				}
			}
		};
	}
}
