/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An enhanced unary operator.
 * @author Paul Ferraro
 * @param <T> the operating type
 */
public interface UnaryOperator<T> extends java.util.function.UnaryOperator<T>, Function<T, T> {
	UnaryOperator<?> IDENTITY = new UnaryOperator<>() {
		@Override
		public Object apply(Object value) {
			return value;
		}
	};
	UnaryOperator<?> NULL = new UnaryOperator<>() {
		@Override
		public Object apply(Object value) {
			return null;
		}
	};

	/**
	 * Returns an operator that applies this function to the value returned by the specified provider if its value does not match the specified predicate.
	 * @param predicate a predicate used to determine the parameter of this function
	 * @param defaultValue a provider of the default parameter value
	 * @return an operator that applies this function to the value returned by the specified provider if its value does not match the specified predicate.
	 */
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
	 * @param defaultResult a provider of the default parameter value
	 * @return an operator that applies this function if its parameter matches the specified predicate, or returns the value provided by the specified supplier otherwise.
	 */
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
	 * @see {@link java.util.function.Function#andThen(java.util.function.Function)}
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
	 * @see {@link java.util.function.Function#compose(java.util.function.Function)}
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
	 * Returns an operator that returns its value.
	 * @param <T> the operating type
	 * @return an operator that returns its value.
	 * @see {@link java.util.function.Function#identity()}
	 */
	@SuppressWarnings("unchecked")
	static <T> UnaryOperator<T> identity() {
		return (UnaryOperator<T>) IDENTITY;
	}

	/**
	 * Returns an operator that always returns the specified value, ignoring its parameter.
	 * @param <T> the operating type
	 * @param value the value returned by the operator
	 * @return an operator that always returns the specified value, ignoring its parameter.
	 */
	@SuppressWarnings("unchecked")
	static <T> UnaryOperator<T> of(T value) {
		return (value != null) ? new UnaryOperator<>() {
			@Override
			public T apply(T ignore) {
				return value;
			}
		} : (UnaryOperator<T>) NULL;
	}

	/**
	 * Returns an operator that returns the result of the specified supplier, ignoring its parameter.
	 * @param <T> the operating type
	 * @param supplier the supplier of the operator result
	 * @return an operator that returns the result of the specified supplier, ignoring its parameter.
	 */
	@SuppressWarnings("unchecked")
	static <T> UnaryOperator<T> of(java.util.function.Supplier<T> supplier) {
		return (supplier != null) && (supplier != Supplier.NULL) ? new UnaryOperator<>() {
			@Override
			public T apply(T ignore) {
				return supplier.get();
			}
		} : (UnaryOperator<T>) NULL;
	}

	/**
	 * Returns an operator view of the specified function.
	 * @param <T> the operating type
	 * @param function the delegating function
	 * @return an operator view of the specified function.
	 */
	@SuppressWarnings("unchecked")
	static <T> UnaryOperator<T> of(java.util.function.Function<? super T, T> function) {
		return (function != null) && (function != Function.NULL) ? new UnaryOperator<>() {
			@Override
			public T apply(T value) {
				return function.apply(value);
			}
		} : (UnaryOperator<T>) NULL;
	}

	static <T> UnaryOperator<T> ifAbsent(java.util.function.Supplier<? extends T> factory) {
		return new UnaryOperator<>() {
			@Override
			public T apply(T value) {
				return (value != null) ? value : factory.get();
			}
		};
	}
}
