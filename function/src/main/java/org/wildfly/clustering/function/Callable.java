/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An enhanced callable.
 * @author Paul Ferraro
 * @param <T> the result type
 */
public interface Callable<T> extends java.util.concurrent.Callable<T> {
	/**
	 * Returns a callable whose result is mapped by the specified function.
	 * @param <R> the mapped result type
	 * @param mapper a mapping function
	 * @return a callable whose result is mapped by the specified function.
	 */
	default <R> Callable<R> map(Function<T, R> mapper) {
		return new Callable<>() {
			@Override
			public R call() throws Exception {
				return mapper.apply(Callable.this.call());
			}
		};
	}

	/**
	 * Returns a callable that runs the specified runner and returns <code>null</code>.
	 * @param runner a runner
	 * @return a callable that runs the specified runner and returns <code>null</code>.
	 */
	static Callable<Void> of(Runnable runner) {
		return new Callable<>() {
			@Override
			public Void call() {
				runner.run();
				return null;
			}
		};
	}

	/**
	 * Returns a callable that delegates to the specified supplier.
	 * @param supplier a supplier
	 * @return the result of the specified supplier.
	 */
	static <T> Callable<T> of(Supplier<T> supplier) {
		return new Callable<>() {
			@Override
			public T call() {
				return supplier.get();
			}
		};
	}

	/**
	 * Returns a callable that returns the specified value.
	 * @param value the result value
	 * @return a callable that returns the specified value.
	 */
	static <T> Callable<T> of(T value) {
		return new Callable<>() {
			@Override
			public T call() {
				return value;
			}
		};
	}
}
