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

	default <R> Callable<R> map(Function<T, R> mapper) {
		return new Callable<>() {
			@Override
			public R call() throws Exception {
				return mapper.apply(Callable.this.call());
			}
		};
	}

	static Callable<Void> of(Runnable runner) {
		return new Callable<>() {
			@Override
			public Void call() {
				runner.run();
				return null;
			}
		};
	}

	static <T> Callable<T> of(Supplier<T> supplier) {
		return new Callable<>() {
			@Override
			public T call() {
				return supplier.get();
			}
		};
	}
}
