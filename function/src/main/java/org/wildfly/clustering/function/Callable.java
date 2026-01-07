/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A exception-producing supplier.
 * @author Paul Ferraro
 * @param <T> the result type
 */
public interface Callable<T> extends java.util.concurrent.Callable<T>, MappableToObjectOperation<T> {

	/**
	 * Composes a supplier from this callable using the specified exception handler.
	 * @param handler an exception handler
	 * @return a supplier that calls this callable
	 */
	default Supplier<T> handle(java.util.function.Function<? super Exception, ? extends T> handler) {
		return new Supplier<>() {
			@Override
			public T get() {
				try {
					return Callable.this.call();
				} catch (Exception e) {
					return handler.apply(e);
				}
			}
		};
	}

	@Override
	default <R> Callable<R> thenApply(java.util.function.Function<? super T, ? extends R> after) {
		return Callable.of(this, after);
	}

	/**
	 * Returns a callable that delegates to the specified supplier.
	 * @param supplier a supplier
	 * @param <T> the result type
	 * @return the result of the specified supplier.
	 */
	static <T> Callable<T> get(java.util.function.Supplier<T> supplier) {
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
	 * @param <T> the result type
	 * @return a callable that returns the specified value.
	 */
	@SuppressWarnings("unchecked")
	static <T> Callable<T> of(T value) {
		return (value != null) ? new Callable<>() {
			@Override
			public T call() {
				return value;
			}
		} : (Callable<T>) SimpleCallable.NULL;
	}

	/**
	 * Composes a callable from the specified operations.
	 * @param <T> the return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite callable
	 */
	static <T> Callable<T> of(java.util.concurrent.Callable<Void> before, java.util.function.Supplier<? extends T> after) {
		return new Callable<>() {
			@Override
			public T call() throws Exception {
				before.call();
				return after.get();
			}
		};
	}

	/**
	 * Composes a callable from the specified operations.
	 * @param <T> the intermediate type
	 * @param <R> the return type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite callable
	 */
	static <T, R> Callable<R> of(java.util.concurrent.Callable<? extends T> before, java.util.function.Function<? super T, ? extends R> after) {
		return new Callable<>() {
			@Override
			public R call() throws Exception {
				return after.apply(before.call());
			}
		};
	}

	/**
	 * Returns a callable that throws the provided exception.
	 * @param exceptionProvider a provider of an exception
	 * @param <T> the result type
	 * @return a callable that throws the provided exception.
	 */
	static <T> Callable<T> exceptional(java.util.function.Supplier<? extends Exception> exceptionProvider) {
		return new Callable<>() {
			@Override
			public T call() throws Exception {
				throw exceptionProvider.get();
			}
		};
	}

	/**
	 * A callable that returns a fixed value.
	 * @param <V> the return type
	 */
	class SimpleCallable<V> implements Callable<V> {
		static final Callable<?> NULL = new SimpleCallable<>(null);

		private final V value;

		SimpleCallable(V value) {
			this.value = value;
		}

		@Override
		public V call() {
			return this.value;
		}
	}
}
