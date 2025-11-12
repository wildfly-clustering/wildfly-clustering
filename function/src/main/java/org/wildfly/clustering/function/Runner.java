/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * An enhanced runnable.
 * @author Paul Ferraro
 */
public interface Runner extends java.lang.Runnable {
	/** A runner that performs no action. */
	Runner EMPTY = () -> {};

	/**
	 * Returns a runner that runs the specified runner after running this runner.
	 * @param after a runner to run after this runner
	 * @return a runner that runs the specified runner after running this runner.
	 */
	default Runner andThen(java.lang.Runnable after) {
		return runAll(List.of(this, after));
	}

	/**
	 * Returns a runner that runs the specified runner before running this runner.
	 * @param before a runner to run before this runner
	 * @return a runner that runs the specified runner before running this runner.
	 */
	default Runner compose(java.lang.Runnable before) {
		return runAll(List.of(before, this));
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
	 * Returns an empty runner.
	 * @return an empty runner.
	 */
	static Runner empty() {
		return EMPTY;
	}

	/**
	 * Returns a runner that throws the provided runtime exception.
	 * @param exceptionProvider a runtime exception provider
	 * @return a runner that throws the provided runtime exception.
	 */
	static Runner throwing(Supplier<RuntimeException> exceptionProvider) {
		return new Runner() {
			@Override
			public void run() {
				throw exceptionProvider.get();
			}
		};
	}

	/**
	 * Returns a runner that consumes a value from the specified supplier.
	 * @param <T> the consumed value
	 * @param consumer a consumer of the supplied value
	 * @param supplier a supplier of the consumed value
	 * @return a runner that consumes a value from the specified supplier.
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
	 * Returns a runner that consumes a value from the specified supplier.
	 * @param consumer a consumer of the supplied value
	 * @param supplier a supplier of the consumed value
	 * @return a runner that consumes a value from the specified supplier.
	 */
	static Runner accept(java.util.function.DoubleConsumer consumer, DoubleSupplier supplier) {
		return new Runner() {
			@Override
			public void run() {
				consumer.accept(supplier.getAsDouble());
			}
		};
	}

	/**
	 * Returns a runner that consumes a value from the specified supplier.
	 * @param consumer a consumer of the supplied value
	 * @param supplier a supplier of the consumed value
	 * @return a runner that consumes a value from the specified supplier.
	 */
	static Runner accept(java.util.function.IntConsumer consumer, IntSupplier supplier) {
		return new Runner() {
			@Override
			public void run() {
				consumer.accept(supplier.getAsInt());
			}
		};
	}

	/**
	 * Returns a runner that consumes a value from the specified supplier.
	 * @param consumer a consumer of the supplied value
	 * @param supplier a supplier of the consumed value
	 * @return a runner that consumes a value from the specified supplier.
	 */
	static Runner accept(java.util.function.LongConsumer consumer, LongSupplier supplier) {
		return new Runner() {
			@Override
			public void run() {
				consumer.accept(supplier.getAsLong());
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
