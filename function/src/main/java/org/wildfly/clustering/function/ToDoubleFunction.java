/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function returning a double value.
 * @author Paul Ferraro
 * @param <V> the parameter type
 */
public interface ToDoubleFunction<V> extends java.util.function.ToDoubleFunction<V>, ToPrimitiveFunction<V, Double>, ToDoubleOperation {

	@Override
	default <T> ToDoubleFunction<T> compose(java.util.function.Function<? super T, ? extends V> before) {
		return ToDoubleFunction.of(before, this);
	}

	@Override
	default <T1, T2> ToDoubleBiFunction<T1, T2> composeBinary(java.util.function.BiFunction<? super T1, ? super T2, ? extends V> before) {
		return ToDoubleBiFunction.of(before, this);
	}

	@Override
	default BooleanToDoubleFunction composeBoolean(BooleanFunction<? extends V> before) {
		return BooleanToDoubleFunction.of(before, this);
	}

	@Override
	default DoubleUnaryOperator composeDouble(java.util.function.DoubleFunction<? extends V> before) {
		return DoubleUnaryOperator.of(before, this);
	}

	@Override
	default IntToDoubleFunction composeInt(java.util.function.IntFunction<? extends V> before) {
		return IntToDoubleFunction.of(before, this);
	}

	@Override
	default LongToDoubleFunction composeLong(java.util.function.LongFunction<? extends V> before) {
		return LongToDoubleFunction.of(before, this);
	}

	@Override
	default Consumer<V> thenAccept(java.util.function.DoubleConsumer after) {
		return Consumer.of(this, after);
	}

	@Override
	default <R> Function<V, R> thenApply(java.util.function.DoubleFunction<? extends R> after) {
		return Function.of(this, after);
	}

	@Override
	default ToDoubleFunction<V> thenApplyAsDouble(java.util.function.DoubleUnaryOperator after) {
		return ToDoubleFunction.of(this, after);
	}

	@Override
	default ToIntFunction<V> thenApplyAsInt(java.util.function.DoubleToIntFunction after) {
		return ToIntFunction.of(this, after);
	}

	@Override
	default ToLongFunction<V> thenApplyAsLong(java.util.function.DoubleToLongFunction after) {
		return ToLongFunction.of(this, after);
	}

	@Override
	default Function<V, Double> thenBox() {
		return this.thenApply(DoubleUnaryOperator.identity().thenBox());
	}

	@Override
	default Predicate<V> thenTest(java.util.function.DoublePredicate after) {
		return Predicate.of(this, after);
	}

	/**
	 * Returns a function returning the specified value, ignoring its parameter.
	 * @param <T> the parameter type
	 * @param value the return value
	 * @return a function returning the specified value, ignoring its parameter.
	 */
	static <T> ToDoubleFunction<T> of(double value) {
		return new ToDoubleFunction<>() {
			@Override
			public double applyAsDouble(T ignore) {
				return value;
			}

			@Override
			public Function<T, Double> thenBox() {
				return Function.of(Double.valueOf(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T> the function parameter type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T> ToDoubleFunction<T> of(java.util.function.Consumer<? super T> before, java.util.function.DoubleSupplier after) {
		return new ToDoubleFunction<>() {
			@Override
			public double applyAsDouble(T value) {
				before.accept(value);
				return after.getAsDouble();
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T> the function parameter type
	 * @param <V> the intermediate type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T, V> ToDoubleFunction<T> of(java.util.function.Function<? super T, ? extends V> before, java.util.function.ToDoubleFunction<? super V> after) {
		return new ToDoubleFunction<>() {
			@Override
			public double applyAsDouble(T value) {
				return after.applyAsDouble(before.apply(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T> the function parameter type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T> ToDoubleFunction<T> of(java.util.function.Predicate<? super T> before, BooleanToDoubleFunction after) {
		return new ToDoubleFunction<>() {
			@Override
			public double applyAsDouble(T value) {
				return after.applyAsDouble(before.test(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T> the function parameter type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T> ToDoubleFunction<T> of(java.util.function.ToDoubleFunction<? super T> before, java.util.function.DoubleUnaryOperator after) {
		return new ToDoubleFunction<>() {
			@Override
			public double applyAsDouble(T value) {
				return after.applyAsDouble(before.applyAsDouble(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T> the function parameter type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T> ToDoubleFunction<T> of(java.util.function.ToIntFunction<? super T> before, java.util.function.IntToDoubleFunction after) {
		return new ToDoubleFunction<>() {
			@Override
			public double applyAsDouble(T value) {
				return after.applyAsDouble(before.applyAsInt(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T> the function parameter type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T> ToDoubleFunction<T> of(java.util.function.ToLongFunction<? super T> before, java.util.function.LongToDoubleFunction after) {
		return new ToDoubleFunction<>() {
			@Override
			public double applyAsDouble(T value) {
				return after.applyAsDouble(before.applyAsLong(value));
			}
		};
	}

	/**
	 * Returns a function that delegates to one of two functions based on the specified predicate.
	 * @param <T> the function parameter type
	 * @param predicate a predicate
	 * @param accepted the function to apply when accepted by the specified predicate
	 * @param rejected the function to apply when rejected by the specified predicate
	 * @return a function that delegates to one of two functions based on the specified predicate.
	 */
	static <T> ToDoubleFunction<T> when(java.util.function.Predicate<? super T> predicate, java.util.function.ToDoubleFunction<? super T> accepted, java.util.function.ToDoubleFunction<? super T> rejected) {
		return new ToDoubleFunction<>() {
			@Override
			public double applyAsDouble(T value) {
				java.util.function.ToDoubleFunction<? super T> function = predicate.test(value) ? accepted : rejected;
				return function.applyAsDouble(value);
			}
		};
	}
}
