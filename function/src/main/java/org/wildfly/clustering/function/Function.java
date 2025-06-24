/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.io.Serializable;

/**
 * An enhanced function.
 * @author Paul Ferraro
 * @param <T> the function parameter type
 * @param <R> the function return type
 */
public interface Function<T, R> extends java.util.function.Function<T, R> {
	Function<?, ?> IDENTITY = new IdentityFunction<>();
	Function<?, ?> NULL = new Function<>() {
		@Override
		public Object apply(Object value) {
			return null;
		}
	};

	@Override
	default <V> Function<V, R> compose(java.util.function.Function<? super V, ? extends T> before) {
		return new Function<>() {
			@Override
			public R apply(V value) {
				return Function.this.apply(before.apply(value));
			}
		};
	}

	@Override
	default <V> Function<T, V> andThen(java.util.function.Function<? super R, ? extends V> after) {
		return new Function<>() {
			@Override
			public V apply(T value) {
				return after.apply(Function.this.apply(value));
			}
		};
	}

	/**
	 * Returns a function that applies this function to the value returned by the specified provider if its value does not match the specified predicate.
	 * @param predicate a predicate used to determine the parameter of this function
	 * @param defaultValue a provider of the default parameter value
	 * @return a function that applies this function to the value returned by the specified provider if its value does not match the specified predicate.
	 */
	default Function<T, R> withDefault(java.util.function.Predicate<T> predicate, java.util.function.Supplier<T> defaultValue) {
		return new Function<>() {
			@Override
			public R apply(T value) {
				return Function.this.apply(predicate.test(value) ? value : defaultValue.get());
			}
		};
	}

	/**
	 * Returns a function that applies this function if its parameter matches the specified predicate, or returns the value provided by the specified supplier otherwise.
	 * @param predicate a predicate used to determine the parameter of this function
	 * @param defaultResult a provider of the default parameter value
	 * @return a function that applies this function if its parameter matches the specified predicate, or returns the value provided by the specified supplier otherwise.
	 */
	default Function<T, R> orDefault(java.util.function.Predicate<T> predicate, java.util.function.Supplier<R> defaultResult) {
		return new Function<>() {
			@Override
			public R apply(T value) {
				return predicate.test(value) ? Function.this.apply(value) : defaultResult.get();
			}
		};
	}

	/**
	 * Returns a function that returns its parameter.
	 * @param <T> the function parameter type
	 * @param <R> the function return type
	 * @return an identity function
	 */
	@SuppressWarnings("unchecked")
	static <T extends R, R> Function<T, R> identity() {
		return (Function<T, R>) IDENTITY;
	}

	/**
	 * Returns a function that returns its parameter.
	 * @param <T> the function parameter type
	 * @param <R> the function return type
	 * @return an identity function
	 */
	@SuppressWarnings("unchecked")
	static <T, R> Function<T, R> empty() {
		return (Function<T, R>) NULL;
	}

	/**
	 * Returns a function that always returns the specified value, ignoring its parameter.
	 * @param <T> the function parameter type
	 * @param <R> the function return type
	 * @param result the function result
	 * @return a function that always returns the specified value, ignoring its parameter.
	 */
	static <T, R> Function<T, R> of(R result) {
		return (result != null) ? new Function<>() {
			@Override
			public R apply(T ignore) {
				return result;
			}
		} : empty();
	}

	/**
	 * Returns a function that returns the value returned by the specified supplier, ignoring its parameter.
	 * @param <T> the function parameter type
	 * @param <R> the function return type
	 * @param supplier the function result supplier
	 * @return a function that returns the value returned by the specified supplier, ignoring its parameter.
	 */
	static <T, R> Function<T, R> get(java.util.function.Supplier<R> supplier) {
		return (supplier != null) && (supplier != Supplier.NULL) ? new Function<>() {
			@Override
			public R apply(T ignore) {
				return supplier.get();
			}
		} : empty();
	}

	class IdentityFunction<T extends R, R> implements Function<T, R>, Serializable {
		private static final long serialVersionUID = 8125088982681052323L;

		@Override
		public R apply(T value) {
			return value;
		}
	}
}
