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
	 * Returns an operator that applies the specified operator to the result of this operator.
	 * @param after an operator to apply to the result of this operation
	 * @return an operator that applies the specified operator to the result of this operator.
	 */
	default UnaryOperator<T> andThen(java.util.function.UnaryOperator<T> after) {
		return new UnaryOperator<>() {
			@Override
			public T apply(T value) {
				return after.apply(UnaryOperator.this.apply(value));
			}
		};
	}

	/**
	 * Returns an operator that applies this operator to the result of the specified operator.
	 * @param before an operator to apply to the result of this operation
	 * @return an operator that applies this operator to the result of the specified operator.
	 */
	default UnaryOperator<T> compose(java.util.function.UnaryOperator<T> before) {
		return new UnaryOperator<>() {
			@Override
			public T apply(T value) {
				return UnaryOperator.this.apply(before.apply(value));
			}
		};
	}

	/**
	 * Returns an operator that applies this operator to the result of the specified operator.
	 * @param before an operator to apply to the result of this operation
	 * @return an operator that applies this operator to the result of the specified operator.
	 */
	default BinaryOperator<T> compose(java.util.function.BinaryOperator<T> before) {
		return new BinaryOperator<>() {
			@Override
			public T apply(T value1, T value2) {
				return UnaryOperator.this.apply(before.apply(value1, value2));
			}
		};
	}

	/**
	 * Returns an operator that returns its value.
	 * @param <T> the operating type
	 * @return an operator that returns its value.
	 */
	@SuppressWarnings("unchecked")
	static <T> UnaryOperator<T> identity() {
		return (UnaryOperator<T>) IdentityUnaryOperator.INSTANCE;
	}

	/**
	 * Returns an operator that always returns the specified value, ignoring its parameter.
	 * @param <T> the operating type
	 * @param value the value returned by the operator
	 * @return an operator that always returns the specified value, ignoring its parameter.
	 */
	@SuppressWarnings("unchecked")
	static <T> UnaryOperator<T> of(T value) {
		return (value != null) ? new SimpleUnaryOperator<>(value) : (UnaryOperator<T>) SimpleUnaryOperator.NULL;
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
	 * Returns a function that delegates to one of two functions based on the specified predicate.
	 * @param <T> the operator type
	 * @param predicate a predicate
	 * @param accepted the function to apply when accepted by the specified predicate
	 * @param rejected the function to apply when rejected by the specified predicate
	 * @return a function that delegates to one of two functions based on the specified predicate.
	 */
	static <T> UnaryOperator<T> when(java.util.function.Predicate<? super T> predicate, java.util.function.UnaryOperator<T> accepted, java.util.function.UnaryOperator<T> rejected) {
		return new UnaryOperator<>() {
			@Override
			public T apply(T value) {
				java.util.function.UnaryOperator<T> function = predicate.test(value) ? accepted : rejected;
				return function.apply(value);
			}
		};
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

	/**
	 * A function that returns a fixed value.
	 * @param <T> the operator type
	 */
	class SimpleUnaryOperator<T> extends SimpleFunction<T, T> implements UnaryOperator<T> {
		static final UnaryOperator<?> NULL = new SimpleUnaryOperator<>(null);

		private SimpleUnaryOperator(T value) {
			super(value);
		}
	}

	/**
	 * A function that returns its parameter.
	 * @param <T> the operator type
	 */
	class IdentityUnaryOperator<T> extends IdentityFunction<T, T> implements UnaryOperator<T> {
		static final UnaryOperator<?> INSTANCE = new IdentityUnaryOperator<>();

		private IdentityUnaryOperator() {
			// Hide
		}
	}
}
