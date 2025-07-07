/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An enhanced binary function.
 * @author Paul Ferraro
 * @param <T> the first parameter type
 * @param <U> the second parameter type
 * @param <R> the result type
 */
public interface BiFunction<T, U, R> extends java.util.function.BiFunction<T, U, R> {
	BiFunction<?, ?, ?> FORMER_IDENTITY = new BiFunction<>() {
		@Override
		public Object apply(Object value1, Object value2) {
			return value1;
		}
	};
	BiFunction<?, ?, ?> LATTER_IDENTITY = new BiFunction<>() {
		@Override
		public Object apply(Object value1, Object value2) {
			return value2;
		}
	};
	BiFunction<?, ?, ?> NULL = new BiFunction<>() {
		@Override
		public Object apply(Object value1, Object value2) {
			return null;
		}
	};

	/**
	 * Returns a composed function that applies the specified functions to each parameter as inputs to this function.
	 * @param <V1> the first parameter function type
	 * @param <V2> the second parameter function type
	 * @param before1 the function applied to the first parameter
	 * @param before2 the function applied to the second parameter
	 * @return a composed function that applies the specified functions to each parameter as inputs to this function.
	 */
	default <V1, V2> BiFunction<V1, V2, R> compose(java.util.function.Function<? super V1, ? extends T> before1, java.util.function.Function<? super V2, ? extends U> before2) {
		return new BiFunction<>() {
			@Override
			public R apply(V1 value1, V2 value2) {
				return BiFunction.this.apply(before1.apply(value1), before2.apply(value2));
			}
		};
	}

	@Override
	default <V> BiFunction<T, U, V> andThen(java.util.function.Function<? super R, ? extends V> after) {
		return new BiFunction<>() {
			@Override
			public V apply(T value1, U value2) {
				return after.apply(BiFunction.this.apply(value1, value2));
			}
		};
	}

	/**
	 * Returns a function that processes this function with reversed parameter order.
	 * @return a function that processes this function with reversed parameter order.
	 */
	default BiFunction<U, T, R> reverse() {
		return new BiFunction<>() {
			@Override
			public R apply(U value2, T value1) {
				return BiFunction.this.apply(value1, value2);
			}
		};
	}

	/**
	 * Returns a function that applies this function to the values returned by the specified providers if its parameters do not match the specified predicates.
	 * @param predicate1 a predicate used to determine the first parameter of this function
	 * @param defaultValue1 a provider of the default value of the first parameter
	 * @param predicate2 a predicate used to determine the second parameter of this function
	 * @param defaultValue2 a provider of the default value of the second parameter
	 * @return a function that applies this function to the value returned by the specified provider if its value does not match the specified predicate.
	 */
	default BiFunction<T, U, R> withDefault(java.util.function.Predicate<T> predicate1, java.util.function.Supplier<T> defaultValue1, java.util.function.Predicate<U> predicate2, java.util.function.Supplier<U> defaultValue2) {
		return new BiFunction<>() {
			@Override
			public R apply(T value1, U value2) {
				return BiFunction.this.apply(predicate1.test(value1) ? value1 : defaultValue1.get(), predicate2.test(value2) ? value2 : defaultValue2.get());
			}
		};
	}

	/**
	 * Returns a function that applies this function if its parameters matches the specified predicate, or returns the value provided by the specified supplier otherwise.
	 * @param predicate a predicate used to determine the parameter of this function
	 * @param defaultResult a provider of the default parameter value
	 * @return a function that applies this function if its parameter matches the specified predicate, or returns the value provided by the specified supplier otherwise.
	 */
	default BiFunction<T, U, R> orDefault(java.util.function.BiPredicate<T, U> predicate, java.util.function.Supplier<R> defaultResult) {
		return new BiFunction<>() {
			@Override
			public R apply(T value1, U value2) {
				return predicate.test(value1, value2) ? BiFunction.this.apply(value1, value2) : defaultResult.get();
			}
		};
	}

	/**
	 * Returns a function that returns its first parameter.
	 * @param <T> the first parameter type
	 * @param <U> the second parameter type
	 * @param <R> the function return type
	 * @return a function that returns its first parameter.
	 */
	@SuppressWarnings("unchecked")
	static <T extends R, U, R> BiFunction<T, U, R> former() {
		return (BiFunction<T, U, R>) FORMER_IDENTITY;
	}

	/**
	 * Returns a function that returns its second parameter.
	 * @param <T> the first parameter type
	 * @param <U> the second parameter type
	 * @param <R> the function return type
	 * @return a function that returns its first parameter.
	 */
	@SuppressWarnings("unchecked")
	static <T, U extends R, R> BiFunction<T, U, R> latter() {
		return (BiFunction<T, U, R>) LATTER_IDENTITY;
	}

	/**
	 * Returns a function that always returns the specified value, ignoring its parameters.
	 * @param <T> the first parameter type
	 * @param <U> the second parameter type
	 * @param <R> the function return type
	 * @param result the function result
	 * @return a function that always returns the specified value, ignoring its parameters.
	 */
	@SuppressWarnings("unchecked")
	static <T, U, R> BiFunction<T, U, R> empty() {
		return (BiFunction<T, U, R>) NULL;
	}

	/**
	 * Returns a function that always returns the specified value, ignoring its parameter.
	 * @param <T> the first parameter type
	 * @param <U> the second parameter type
	 * @param <R> the function return type
	 * @param result the function result
	 * @return a function that always returns the specified value, ignoring its parameter.
	 */
	static <T, U, R> BiFunction<T, U, R> of(R result) {
		return (result != null) ? get(Supplier.of(result)) : empty();
	}

	/**
	 * Returns a function that returns the value returned by the specified supplier, ignoring its parameter.
	 * @param <T> the first parameter type
	 * @param <U> the second parameter type
	 * @param <R> the function return type
	 * @param supplier the function result supplier
	 * @return a function that returns the value returned by the specified supplier, ignoring its parameter.
	 */
	static <T, U, R> BiFunction<T, U, R> get(java.util.function.Supplier<R> supplier) {
		return (supplier != null) && (supplier != Supplier.NULL) ? new BiFunction<>() {
			@Override
			public R apply(T ignore1, U ignore2) {
				return supplier.get();
			}
		} : empty();
	}
}
