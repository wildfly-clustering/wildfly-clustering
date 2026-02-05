/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.AbstractMap;
import java.util.Map;

/**
 * An enhanced unary operator.
 * @author Paul Ferraro
 * @param <T> the operating type
 */
public interface UnaryOperator<T> extends java.util.function.UnaryOperator<T>, Function<T, T> {

	/**
	 * Returns an operator that applies this function to the value returned by the specified provider if its value does not match the specified predicate.
	 * @param predicate a predicate used to determine the parameter of this function
	 * @param defaultResult a provider of the default operation result
	 * @return an operator that applies this function to the value returned by the specified provider if its value does not match the specified predicate.
	 */
	@Override
	default UnaryOperator<T> orDefault(java.util.function.Predicate<T> predicate, java.util.function.Supplier<T> defaultResult) {
		return new UnaryOperator<>() {
			@Override
			public T apply(T value) {
				return predicate.test(value) ? UnaryOperator.this.apply(value) : defaultResult.get();
			}
		};
	}

	/**
	 * Returns an operator that applies this function if its parameter matches the specified predicate, or returns the value provided by the specified supplier otherwise.
	 * @param predicate a predicate used to determine the parameter of this function
	 * @param defaultValue a provider of the default parameter value
	 * @return an operator that applies this function if its parameter matches the specified predicate, or returns the value provided by the specified supplier otherwise.
	 */
	@Override
	default UnaryOperator<T> withDefault(java.util.function.Predicate<T> predicate, java.util.function.Supplier<T> defaultValue) {
		return new UnaryOperator<>() {
			@Override
			public T apply(T value) {
				return UnaryOperator.this.apply(predicate.test(value) ? value : defaultValue.get());
			}
		};
	}

	/**
	 * Returns an operator that applies the specified operator to the result of this operator.
	 * @param operator an operator to apply to the result of this operation
	 * @return an operator that applies the specified operator to the result of this operator.
	 */
	default UnaryOperator<T> andThen(java.util.function.UnaryOperator<T> operator) {
		return new UnaryOperator<>() {
			@Override
			public T apply(T value) {
				return operator.apply(UnaryOperator.this.apply(value));
			}
		};
	}

	/**
	 * Returns an operator that applies this operator to the result of the specified operator.
	 * @param operator an operator to apply to the result of this operation
	 * @return an operator that applies this operator to the result of the specified operator.
	 */
	default UnaryOperator<T> compose(java.util.function.UnaryOperator<T> operator) {
		return new UnaryOperator<>() {
			@Override
			public T apply(T value) {
				return UnaryOperator.this.apply(operator.apply(value));
			}
		};
	}

	/**
	 * Returns a new operator that delegates to this operator using the specified exception handler.
	 * @param handler an exception handler
	 * @return a new operator that delegates to this operator using the specified exception handler.
	 */
	@Override
	default UnaryOperator<T> handle(java.util.function.BiFunction<T, RuntimeException, T> handler) {
		return new UnaryOperator<>() {
			@Override
			public T apply(T value) {
				try {
					return UnaryOperator.this.apply(value);
				} catch (RuntimeException e) {
					return handler.apply(value, e);
				}
			}
		};
	}

	/**
	 * Returns an operator that returns its value.
	 * @param <T> the operating type
	 * @return an operator that returns its value.
	 */
	static <T> UnaryOperator<T> identity() {
		return UnaryOperators.IDENTITY.cast();
	}

	/**
	 * Returns an operator that always returns null, ignoring its parameter.
	 * @param <T> the operating type
	 * @return an operator that always returns null, ignoring its parameter.
	 */
	static <T> UnaryOperator<T> empty() {
		return UnaryOperators.NULL.cast();
	}

	/**
	 * Returns an operator that always returns the specified value, ignoring its parameter.
	 * @param <T> the operating type
	 * @param value the value returned by the operator
	 * @return an operator that always returns the specified value, ignoring its parameter.
	 */
	static <T> UnaryOperator<T> of(T value) {
		return (value != null) ? of(Consumer.empty(), Supplier.of(value)) : empty();
	}

	/**
	 * Returns an operator that accepts its parameter via the specified consumer and returns the value returned by the specified supplier.
	 * @param <T> the operator type
	 * @param consumer a consumer of the operator parameter
	 * @param supplier the supplier of the operator result
	 * @return an operator that accepts its parameter via the specified consumer and returns the value returned by the specified supplier.
	 */
	static <T> UnaryOperator<T> of(java.util.function.Consumer<T> consumer, java.util.function.Supplier<T> supplier) {
		return new UnaryOperator<>() {
			@Override
			public T apply(T value) {
				consumer.accept(value);
				return supplier.get();
			}
		};
	}

	/**
	 * Returns an operator view of the specified function.
	 * @param <T> the operating type
	 * @param function the delegating function
	 * @return an operator view of the specified function.
	 */
	static <T> UnaryOperator<T> apply(java.util.function.Function<? super T, T> function) {
		return (function != null) && (function != Function.empty()) ? new UnaryOperator<>() {
			@Override
			public T apply(T value) {
				return function.apply(value);
			}
		} : empty();
	}

	/**
	 * Returns a {@link java.util.Map.Entry} function from the specified key and value functions.
	 * @param <K> the entry key type
	 * @param <V> the entry value type
	 * @param keyFunction an entry key function
	 * @param valueFunction an entry value function
	 * @return a {@link java.util.Map.Entry} function from the specified key and value functions.
	 */
	static <K, V> UnaryOperator<Map.Entry<K, V>> entry(UnaryOperator<K> keyFunction, UnaryOperator<V> valueFunction) {
		return new UnaryOperator<>() {
			@Override
			public Map.Entry<K, V> apply(Map.Entry<K, V> entry) {
				return new AbstractMap.SimpleImmutableEntry<>(keyFunction.apply(entry.getKey()), valueFunction.apply(entry.getValue()));
			}
		};
	}
}
