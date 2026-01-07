/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A binary function that operates on a single type.
 * @author Paul Ferraro
 * @param <T> the operator type
 */
public interface BinaryOperator<T> extends java.util.function.BinaryOperator<T>, BiFunction<T, T, T> {

	/**
	 * Returns a function that returns its first parameter.
	 * @param <T> the operating type
	 * @return a function that returns its first parameter.
	 */
	@SuppressWarnings("unchecked")
	static <T> BinaryOperator<T> former() {
		return (BinaryOperator<T>) FormerBinaryOperator.INSTANCE;
	}

	/**
	 * Returns a function that returns its second parameter.
	 * @param <T> the operating type
	 * @return a function that returns its first parameter.
	 */
	@SuppressWarnings("unchecked")
	static <T> BinaryOperator<T> latter() {
		return (BinaryOperator<T>) LatterBinaryOperator.INSTANCE;
	}

	/**
	 * Returns a function that always returns the specified value, ignoring its parameter.
	 * @param <T> the operating type
	 * @param result the function result
	 * @return a function that always returns the specified value, ignoring its parameter.
	 */
	@SuppressWarnings("unchecked")
	static <T> BinaryOperator<T> of(T result) {
		return (result != null) ? of(BiConsumer.of(), Supplier.of(result)) : (BinaryOperator<T>) SimpleBinaryOperator.NULL;
	}

	/**
	 * Returns an operator that accepts its parameters via the specified consumer and returns the value returned by the specified supplier.
	 * @param <T> the operator type
	 * @param consumer the consumer of the function parameter
	 * @param supplier the supplier of the function result
	 * @return an operator that accepts its parameters via the specified consumer and returns the value returned by the specified supplier.
	 */
	static <T> BinaryOperator<T> of(java.util.function.BiConsumer<? super T, ? super T> consumer, java.util.function.Supplier<? extends T> supplier) {
		return new BinaryOperator<>() {
			@Override
			public T apply(T value1, T value2) {
				consumer.accept(value1, value2);
				return supplier.get();
			}
		};
	}

	/**
	 * Returns an operator that applies the former parameter to the specified operator.
	 * @param <T> the operating type
	 * @param operator the operator applied to the former parameter
	 * @return an operator that applies the former parameter to the specified operator.
	 */
	static <T> BinaryOperator<T> former(java.util.function.UnaryOperator<T> operator) {
		return new FormerBinaryOperator<>(operator);
	}

	/**
	 * Returns an operator that applies the latter parameter to the specified operator.
	 * @param <T> the operating type
	 * @param operator the operator applied to the latter parameter
	 * @return an operator that applies the latter parameter to the specified operator.
	 */
	static <T> BinaryOperator<T> latter(java.util.function.UnaryOperator<T> operator) {
		return new LatterBinaryOperator<>(operator);
	}

	/**
	 * Returns a function that delegates to one of two functions based on the specified predicate.
	 * @param <T> the operating type
	 * @param predicate a predicate
	 * @param accepted the function to apply when accepted by the specified predicate
	 * @param rejected the function to apply when rejected by the specified predicate
	 * @return a function that delegates to one of two functions based on the specified predicate.
	 */
	static <T> BinaryOperator<T> when(java.util.function.BiPredicate<T, T> predicate, java.util.function.BinaryOperator<T> accepted, java.util.function.BinaryOperator<T> rejected) {
		return new BinaryOperator<>() {
			@Override
			public T apply(T value1, T value2) {
				java.util.function.BinaryOperator<T> function = predicate.test(value1, value2) ? accepted : rejected;
				return function.apply(value1, value2);
			}
		};
	}

	/**
	 * A function that returns a fixed value.
	 * @param <V> the operating type
	 */
	class SimpleBinaryOperator<V> extends SimpleBiFunction<V, V, V> implements BinaryOperator<V> {
		static final BinaryOperator<?> NULL = new SimpleBinaryOperator<>(null);

		SimpleBinaryOperator(V value) {
			super(value);
		}
	}

	/**
	 * A function that returns its former parameter.
	 * @param <V> the operating type
	 */
	class FormerBinaryOperator<V> extends FormerBiFunction<V, V, V> implements BinaryOperator<V> {
		static final BinaryOperator<?> INSTANCE = new FormerBinaryOperator<>(UnaryOperator.identity());

		FormerBinaryOperator(java.util.function.UnaryOperator<V> operator) {
			super(operator);
		}
	}

	/**
	 * A function that returns its latter parameter.
	 * @param <V> the operating type
	 */
	class LatterBinaryOperator<V> extends LatterBiFunction<V, V, V> implements BinaryOperator<V> {
		static final BinaryOperator<?> INSTANCE = new LatterBinaryOperator<>(UnaryOperator.identity());

		LatterBinaryOperator(java.util.function.UnaryOperator<V> operator) {
			super(operator);
		}
	}
}
