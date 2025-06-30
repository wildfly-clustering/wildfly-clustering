/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An enhanced supplier.
 * @author Paul Ferraro
 * @param <T> the supplied type
 */
public interface Supplier<T> extends java.util.function.Supplier<T> {
	Supplier<?> NULL = new Supplier<>() {
		@Override
		public Object get() {
			return null;
		}
	};

	/**
	 * Returns a supplier that returns the value this function mapped via the specified function.
	 * @param <V> the mapped value type
	 * @param mapper a mapping function
	 * @return a supplier that returns the value this function mapped via the specified function.
	 */
	default <V> Supplier<V> map(java.util.function.Function<T, V> mapper) {
		return new Supplier<>() {
			@Override
			public V get() {
				return mapper.apply(Supplier.this.get());
			}
		};
	}

	/**
	 * Returns a new supplier that delegates to this supplier using the specified exception handler.
	 * @param handler an exception handler
	 * @return a new supplier that delegates to this supplier using the specified exception handler.
	 */
	default Supplier<T> handle(java.util.function.Function<RuntimeException, T> handler) {
		return new Supplier<>() {
			@Override
			public T get() {
				try {
					return Supplier.this.get();
				} catch (RuntimeException e) {
					return handler.apply(e);
				}
			}
		};
	}

	/**
	 * Returns a supplier that always returns the specified value.
	 * @param <T> the supplied type
	 * @param value the supplied value
	 * @return a supplier that always returns the specified value.
	 */
	@SuppressWarnings("unchecked")
	static <T> Supplier<T> empty() {
		return (Supplier<T>) NULL;
	}

	/**
	 * Returns a supplier that always returns the specified value.
	 * @param <T> the supplied type
	 * @param value the supplied value
	 * @return a supplier that always returns the specified value.
	 */
	static <T> Supplier<T> of(T value) {
		return (value != null) ? new Supplier<>() {
			@Override
			public T get() {
				return value;
			}
		} : empty();
	}

	/**
	 * Returns a supplier that returns null after invoking the specified task.
	 * @param <T> the supplied type
	 * @param task the task to run
	 * @return a supplier that returns null after invoking the specified task.
	 */
	static <T> Supplier<T> run(java.lang.Runnable task) {
		return (task != null) ? new Supplier<>() {
			@Override
			public T get() {
				task.run();
				return null;
			}
		} : empty();
	}

	/**
	 * Returns a supplier that delegates to the specified caller using the specified exception handler.
	 * @param <T> the supplied type
	 * @param caller the caller to call
	 * @param handler an exception handler
	 * @return a supplier that delegates to the specified caller using the specified exception handler.
	 */
	static <T> Supplier<T> call(java.util.concurrent.Callable<T> caller, java.util.function.Function<Exception, T> handler) {
		return (caller != null) ? new Supplier<>() {
			@Override
			public T get() {
				try {
					return caller.call();
				} catch (Exception e) {
					return handler.apply(e);
				}
			}
		} : empty();
	}
}
