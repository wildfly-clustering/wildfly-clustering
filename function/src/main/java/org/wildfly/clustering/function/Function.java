/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;

/**
 * An enhanced function.
 * @author Paul Ferraro
 * @param <T> the function parameter type
 * @param <R> the function return type
 */
public interface Function<T, R> extends java.util.function.Function<T, R> {
	/** An identity function that always returns its parameter */
	Function<?, ?> IDENTITY = value -> value;
	/** A function that always returns null. */
	Function<?, ?> NULL = of(Consumer.EMPTY, Supplier.NULL);

	@Override
	default <V> Function<V, R> compose(java.util.function.Function<? super V, ? extends T> before) {
		return new Function<>() {
			@Override
			public R apply(V value) {
				return Function.this.apply(before.apply(value));
			}
		};
	}

	/**
	 * Composes a binary function that invokes this function using result of the specified binary function.
	 * @param <V1> the former parameter type
	 * @param <V2> the latter parameter type
	 * @param mapper a mapping function
	 * @return a binary function that invokes this function using result of the specified binary function.
	 */
	default <V1, V2> BiFunction<V1, V2, R> compose(BiFunction<V1, V2, T> mapper) {
		return new BiFunction<>() {
			@Override
			public R apply(V1 value1, V2 value2) {
				return Function.this.apply(mapper.apply(value1, value2));
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
	 * Returns a new function that delegates to this function using the specified exception handler.
	 * @param handler an exception handler
	 * @return a new function that delegates to this function using the specified exception handler.
	 */
	default Function<T, R> handle(java.util.function.BiFunction<T, RuntimeException, R> handler) {
		return new Function<>() {
			@Override
			public R apply(T value) {
				try {
					return Function.this.apply(value);
				} catch (RuntimeException e) {
					return handler.apply(value, e);
				}
			}
		};
	}

	/**
	 * Returns an optional function that applies this function to an optional value.
	 * @return an optional function that applies this function to an optional value.
	 */
	default Function<Optional<T>, Optional<R>> optional() {
		return new Function<>() {
			@Override
			public Optional<R> apply(Optional<T> value) {
				return value.map(Function.this);
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
		return (result != null) ? of(Consumer.empty(), Supplier.of(result)) : empty();
	}

	/**
	 * Returns a function that accepts its parameter via the specified consumer and returns the value returned by the specified supplier.
	 * @param <T> the function parameter type
	 * @param <R> the function return type
	 * @param consumer the consumer of the function parameter
	 * @param supplier the supplier of the function result
	 * @return a function that accepts its parameter via the specified consumer and returns the value returned by the specified supplier.
	 */
	static <T, R> Function<T, R> of(java.util.function.Consumer<T> consumer, java.util.function.Supplier<R> supplier) {
		return new Function<>() {
			@Override
			public R apply(T value) {
				consumer.accept(value);
				return supplier.get();
			}
		};
	}

	/**
	 * Returns a {@link java.util.Map.Entry} function from the specified key and value functions.
	 * @param <K> the entry key type
	 * @param <V> the entry value type
	 * @param <KR> the mapped entry key type
	 * @param <VR> the mapped entry value type
	 * @param keyFunction an entry key function
	 * @param valueFunction an entry value function
	 * @return a {@link java.util.Map.Entry} function from the specified key and value functions.
	 */
	static <K, V, KR, VR> Function<Map.Entry<K, V>, Map.Entry<KR, VR>> entry(Function<K, KR> keyFunction, Function<V, VR> valueFunction) {
		return new Function<>() {
			@Override
			public Map.Entry<KR, VR> apply(Map.Entry<K, V> entry) {
				return new AbstractMap.SimpleImmutableEntry<>(keyFunction.apply(entry.getKey()), valueFunction.apply(entry.getValue()));
			}
		};
	}
}
