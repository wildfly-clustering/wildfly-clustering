/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An enhanced binary function.
 * @author Paul Ferraro
 * @param <T> the operator type
 */
public interface BinaryOperator<T> extends java.util.function.BinaryOperator<T>, BiFunction<T, T, T> {
	BinaryOperator<?> FORMER_IDENTITY = new FormerIdentityOperator<>();
	BinaryOperator<?> LATTER_IDENTITY = new LatterIdentityOperator<>();
	BinaryOperator<?> NULL = new BinaryOperator<>() {
		@Override
		public Object apply(Object value1, Object value2) {
			return null;
		}
	};

	/**
	 * Returns a composed operator that applies the specified operators to each parameter as inputs to this operator.
	 * @param before1 the operator applied to the first parameter
	 * @param before2 the operator applied to the second parameter
	 * @return a composed operator that applies the specified operators to each parameter as inputs to this operator.
	 */
	default BinaryOperator<T> compose(java.util.function.UnaryOperator<T> before1, java.util.function.UnaryOperator<T> before2) {
		return new BinaryOperator<>() {
			@Override
			public T apply(T value1, T value2) {
				return BinaryOperator.this.apply(before1.apply(value1), before2.apply(value2));
			}
		};
	}

	default BinaryOperator<T> andThen(java.util.function.UnaryOperator<T> after) {
		return new BinaryOperator<>() {
			@Override
			public T apply(T value1, T value2) {
				return after.apply(BinaryOperator.this.apply(value1, value2));
			}
		};
	}

	/**
	 * Returns a function that processes this function with reversed parameter order.
	 * @return a function that processes this function with reversed parameter order.
	 */
	default BinaryOperator<T> reverse() {
		return new BinaryOperator<>() {
			@Override
			public T apply(T value2, T value1) {
				return BinaryOperator.this.apply(value1, value2);
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
	default BinaryOperator<T> withDefault(java.util.function.Predicate<T> predicate1, java.util.function.Supplier<T> defaultValue1, java.util.function.Predicate<T> predicate2, java.util.function.Supplier<T> defaultValue2) {
		return new BinaryOperator<>() {
			@Override
			public T apply(T value1, T value2) {
				return BinaryOperator.this.apply(predicate1.test(value1) ? value1 : defaultValue1.get(), predicate2.test(value2) ? value2 : defaultValue2.get());
			}
		};
	}

	/**
	 * Returns a function that applies this function if its parameters matches the specified predicate, or returns the value provided by the specified supplier otherwise.
	 * @param predicate a predicate used to determine the parameter of this function
	 * @param defaultResult a provider of the default parameter value
	 * @return a function that applies this function if its parameter matches the specified predicate, or returns the value provided by the specified supplier otherwise.
	 */
	default BinaryOperator<T> orDefault(java.util.function.BiPredicate<T, T> predicate, java.util.function.Supplier<T> defaultResult) {
		return new BinaryOperator<>() {
			@Override
			public T apply(T value1, T value2) {
				return predicate.test(value1, value2) ? BinaryOperator.this.apply(value1, value2) : defaultResult.get();
			}
		};
	}

	/**
	 * Returns a function that returns its first parameter.
	 * @param <T> the operating type
	 * @return a function that returns its first parameter.
	 */
	@SuppressWarnings("unchecked")
	static <T> BinaryOperator<T> former() {
		return (BinaryOperator<T>) FORMER_IDENTITY;
	}

	/**
	 * Returns a function that returns its second parameter.
	 * @param <T> the operating type
	 * @return a function that returns its first parameter.
	 */
	@SuppressWarnings("unchecked")
	static <T> BinaryOperator<T> latter() {
		return (BinaryOperator<T>) LATTER_IDENTITY;
	}

	/**
	 * Returns a function that always returns the specified value, ignoring its parameter.
	 * @param <T> the operating type
	 * @param result the function result
	 * @return a function that always returns the specified value, ignoring its parameter.
	 */
	@SuppressWarnings("unchecked")
	static <T> BinaryOperator<T> of(T result) {
		return (result != null) ? of(Supplier.of(result)) : (BinaryOperator<T>) NULL;
	}

	/**
	 * Returns a function that returns the value returned by the specified supplier, ignoring its parameter.
	 * @param <T> the operating type
	 * @param supplier the function result supplier
	 * @return a function that returns the value returned by the specified supplier, ignoring its parameter.
	 */
	@SuppressWarnings("unchecked")
	static <T> BinaryOperator<T> of(java.util.function.Supplier<T> supplier) {
		return (supplier != null) && (supplier != Supplier.NULL) ? new BinaryOperator<>() {
			@Override
			public T apply(T ignore1, T ignore2) {
				return supplier.get();
			}
		} : (BinaryOperator<T>) BinaryOperator.NULL;
	}

	class FormerIdentityOperator<T> extends BiFunction.FormerIdentityFunction<T, T, T> implements BinaryOperator<T> {
		private static final long serialVersionUID = 1776702302523048465L;
	}

	class LatterIdentityOperator<T> extends BiFunction.LatterIdentityFunction<T, T, T> implements BinaryOperator<T> {
		private static final long serialVersionUID = -8741076230246655393L;
	}
}
