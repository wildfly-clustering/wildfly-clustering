/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function returning an int value.
 * @author Paul Ferraro
 * @param <V> the parameter type
 */
public interface ToIntFunction<V> extends java.util.function.ToIntFunction<V>, ToPrimitiveFunction<V, Integer>, ToIntOperation {

	@Override
	default <T> ToIntFunction<T> compose(java.util.function.Function<? super T, ? extends V> before) {
		return ToIntFunction.of(before, this);
	}

	@Override
	default <T1, T2> ToIntBiFunction<T1, T2> composeBinary(java.util.function.BiFunction<? super T1, ? super T2, ? extends V> before) {
		return ToIntBiFunction.of(before, this);
	}

	@Override
	default BooleanToIntFunction composeBoolean(BooleanFunction<? extends V> before) {
		return BooleanToIntFunction.of(before, this);
	}

	@Override
	default DoubleToIntFunction composeDouble(java.util.function.DoubleFunction<? extends V> before) {
		return DoubleToIntFunction.of(before, this);
	}

	@Override
	default IntUnaryOperator composeInt(java.util.function.IntFunction<? extends V> before) {
		return IntUnaryOperator.of(before, this);
	}

	@Override
	default LongToIntFunction composeLong(java.util.function.LongFunction<? extends V> before) {
		return LongToIntFunction.of(before, this);
	}

	@Override
	default Consumer<V> thenAccept(java.util.function.IntConsumer after) {
		return Consumer.of(this, after);
	}

	@Override
	default Predicate<V> thenTest(java.util.function.IntPredicate after) {
		return Predicate.of(this, after);
	}

	@Override
	default <R> Function<V, R> thenApply(java.util.function.IntFunction<? extends R> after) {
		return Function.of(this, after);
	}

	@Override
	default ToDoubleFunction<V> thenApplyAsDouble(java.util.function.IntToDoubleFunction after) {
		return ToDoubleFunction.of(this, after);
	}

	@Override
	default ToIntFunction<V> thenApplyAsInt(java.util.function.IntUnaryOperator after) {
		return ToIntFunction.of(this, after);
	}

	@Override
	default ToLongFunction<V> thenApplyAsLong(java.util.function.IntToLongFunction after) {
		return ToLongFunction.of(this, after);
	}

	@Override
	default Function<V, Integer> thenBox() {
		return this.thenApply(IntUnaryOperator.identity().thenBox());
	}

	/**
	 * Returns a function returning the specified value, ignoring its parameter.
	 * @param <T> the parameter type
	 * @param value the return value
	 * @return a function returning the specified value, ignoring its parameter.
	 */
	static <T> ToIntFunction<T> of(int value) {
		return new ToIntFunction<>() {
			@Override
			public int applyAsInt(T ignore) {
				return value;
			}

			@Override
			public Function<T, Integer> thenBox() {
				return Function.of(Integer.valueOf(value));
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
	static <T> ToIntFunction<T> of(java.util.function.Consumer<? super T> before, java.util.function.IntSupplier after) {
		return new ToIntFunction<>() {
			@Override
			public int applyAsInt(T value) {
				before.accept(value);
				return after.getAsInt();
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
	static <T, V> ToIntFunction<T> of(java.util.function.Function<? super T, ? extends V> before, java.util.function.ToIntFunction<? super V> after) {
		return new ToIntFunction<>() {
			@Override
			public int applyAsInt(T value) {
				return after.applyAsInt(before.apply(value));
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
	static <T> ToIntFunction<T> of(java.util.function.Predicate<? super T> before, BooleanToIntFunction after) {
		return new ToIntFunction<>() {
			@Override
			public int applyAsInt(T value) {
				return after.applyAsInt(before.test(value));
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
	static <T> ToIntFunction<T> of(java.util.function.ToDoubleFunction<? super T> before, java.util.function.DoubleToIntFunction after) {
		return new ToIntFunction<>() {
			@Override
			public int applyAsInt(T value) {
				return after.applyAsInt(before.applyAsDouble(value));
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
	static <T> ToIntFunction<T> of(java.util.function.ToIntFunction<? super T> before, java.util.function.IntUnaryOperator after) {
		return new ToIntFunction<>() {
			@Override
			public int applyAsInt(T value) {
				return after.applyAsInt(before.applyAsInt(value));
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
	static <T> ToIntFunction<T> of(java.util.function.ToLongFunction<? super T> before, java.util.function.LongToIntFunction after) {
		return new ToIntFunction<>() {
			@Override
			public int applyAsInt(T value) {
				return after.applyAsInt(before.applyAsLong(value));
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
	static <T> ToIntFunction<T> when(java.util.function.Predicate<? super T> predicate, java.util.function.ToIntFunction<? super T> accepted, java.util.function.ToIntFunction<? super T> rejected) {
		return new ToIntFunction<>() {
			@Override
			public int applyAsInt(T value) {
				java.util.function.ToIntFunction<? super T> function = predicate.test(value) ? accepted : rejected;
				return function.applyAsInt(value);
			}
		};
	}
}
