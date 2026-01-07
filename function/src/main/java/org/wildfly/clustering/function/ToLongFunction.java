/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function returning a long value.
 * @author Paul Ferraro
 * @param <V> the parameter type
 */
public interface ToLongFunction<V> extends java.util.function.ToLongFunction<V>, ToPrimitiveFunction<V, Long>, ToLongOperation {

	@Override
	default <T> ToLongFunction<T> compose(java.util.function.Function<? super T, ? extends V> before) {
		return ToLongFunction.of(before, this);
	}

	@Override
	default <V1, V2> ToLongBiFunction<V1, V2> composeBinary(java.util.function.BiFunction<? super V1, ? super V2, ? extends V> before) {
		return ToLongBiFunction.of(before, this);
	}

	@Override
	default BooleanToLongFunction composeBoolean(BooleanFunction<? extends V> before) {
		return BooleanToLongFunction.of(before, this);
	}

	@Override
	default DoubleToLongFunction composeDouble(java.util.function.DoubleFunction<? extends V> before) {
		return DoubleToLongFunction.of(before, this);
	}

	@Override
	default IntToLongFunction composeInt(java.util.function.IntFunction<? extends V> before) {
		return IntToLongFunction.of(before, this);
	}

	@Override
	default LongUnaryOperator composeLong(java.util.function.LongFunction<? extends V> before) {
		return LongUnaryOperator.of(before, this);
	}

	@Override
	default Predicate<V> thenTest(java.util.function.LongPredicate after) {
		return Predicate.of(this, after);
	}

	@Override
	default Consumer<V> thenAccept(java.util.function.LongConsumer after) {
		return Consumer.of(this, after);
	}

	@Override
	default <R> Function<V, R> thenApply(java.util.function.LongFunction<? extends R> after) {
		return Function.of(this, after);
	}

	@Override
	default ToDoubleFunction<V> thenApplyAsDouble(java.util.function.LongToDoubleFunction after) {
		return ToDoubleFunction.of(this, after);
	}

	@Override
	default ToIntFunction<V> thenApplyAsInt(java.util.function.LongToIntFunction after) {
		return ToIntFunction.of(this, after);
	}

	@Override
	default ToLongFunction<V> thenApplyAsLong(java.util.function.LongUnaryOperator after) {
		return ToLongFunction.of(this, after);
	}

	@Override
	default Function<V, Long> thenBox() {
		return this.thenApply(LongUnaryOperator.identity().thenBox());
	}

	/**
	 * Returns a function returning the specified value, ignoring its parameter.
	 * @param <T> the parameter type
	 * @param value the return value
	 * @return a function returning the specified value, ignoring its parameter.
	 */
	static <T> ToLongFunction<T> of(long value) {
		return new ToLongFunction<>() {
			@Override
			public long applyAsLong(T ignore) {
				return value;
			}

			@Override
			public Function<T, Long> thenBox() {
				return Function.of(Long.valueOf(value));
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
	static <T> ToLongFunction<T> of(java.util.function.Consumer<? super T> before, java.util.function.LongSupplier after) {
		return new ToLongFunction<>() {
			@Override
			public long applyAsLong(T value) {
				before.accept(value);
				return after.getAsLong();
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
	static <T, V> ToLongFunction<T> of(java.util.function.Function<? super T, ? extends V> before, java.util.function.ToLongFunction<? super V> after) {
		return new ToLongFunction<>() {
			@Override
			public long applyAsLong(T value) {
				return after.applyAsLong(before.apply(value));
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
	static <T> ToLongFunction<T> of(java.util.function.Predicate<? super T> before, BooleanToLongFunction after) {
		return new ToLongFunction<>() {
			@Override
			public long applyAsLong(T value) {
				return after.applyAsLong(before.test(value));
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
	static <T> ToLongFunction<T> of(java.util.function.ToDoubleFunction<? super T> before, java.util.function.DoubleToLongFunction after) {
		return new ToLongFunction<>() {
			@Override
			public long applyAsLong(T value) {
				return after.applyAsLong(before.applyAsDouble(value));
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
	static <T> ToLongFunction<T> of(java.util.function.ToIntFunction<? super T> before, java.util.function.IntToLongFunction after) {
		return new ToLongFunction<>() {
			@Override
			public long applyAsLong(T value) {
				return after.applyAsLong(before.applyAsInt(value));
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
	static <T> ToLongFunction<T> of(java.util.function.ToLongFunction<? super T> before, java.util.function.LongUnaryOperator after) {
		return new ToLongFunction<>() {
			@Override
			public long applyAsLong(T value) {
				return after.applyAsLong(before.applyAsLong(value));
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
	static <T> ToLongFunction<T> when(java.util.function.Predicate<? super T> predicate, java.util.function.ToLongFunction<? super T> accepted, java.util.function.ToLongFunction<? super T> rejected) {
		return new ToLongFunction<>() {
			@Override
			public long applyAsLong(T value) {
				java.util.function.ToLongFunction<? super T> function = predicate.test(value) ? accepted : rejected;
				return function.applyAsLong(value);
			}
		};
	}
}
