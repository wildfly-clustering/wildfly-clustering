/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.Map;

/**
 * A binary function returning a long value
 * @author Paul Ferraro
 * @param <V1> the former parameter type
 * @param <V2> the latter parameter type
 */
public interface ToLongBiFunction<V1, V2> extends java.util.function.ToLongBiFunction<V1, V2>, BinaryObjectOperation<V1, V2>, ToLongOperation {

	@Override
	default <T1, T2> ToLongBiFunction<T1, T2> compose(java.util.function.Function<? super T1, ? extends V1> beforeFormer, java.util.function.Function<? super T2, ? extends V2> beforeLatter) {
		return new ToLongBiFunction<>() {
			@Override
			public long applyAsLong(T1 value1, T2 value2) {
				return ToLongBiFunction.this.applyAsLong(beforeFormer.apply(value1), beforeLatter.apply(value2));
			}
		};
	}

	@Override
	default ToLongFunction<Map.Entry<V1, V2>> composeEntry() {
		return this.composeUnary(Map.Entry::getKey, Map.Entry::getValue);
	}

	@Override
	default <T> ToLongFunction<T> composeUnary(java.util.function.Function<? super T, ? extends V1> beforeFormer, java.util.function.Function<? super T, ? extends V2> beforeLatter) {
		return new ToLongFunction<>() {
			@Override
			public long applyAsLong(T value) {
				return ToLongBiFunction.this.applyAsLong(beforeFormer.apply(value), beforeLatter.apply(value));
			}
		};
	}

	@Override
	default ToLongBiFunction<V2, V1> reverse() {
		return new ToLongBiFunction<>() {
			@Override
			public long applyAsLong(V2 value2, V1 value1) {
				return ToLongBiFunction.this.applyAsLong(value1, value2);
			}
		};
	}

	@Override
	default BiConsumer<V1, V2> thenAccept(java.util.function.LongConsumer after) {
		return BiConsumer.of(this, after);
	}

	@Override
	default <R> BiFunction<V1, V2, R> thenApply(java.util.function.LongFunction<? extends R> after) {
		return BiFunction.of(this, after);
	}

	@Override
	default ToDoubleBiFunction<V1, V2> thenApplyAsDouble(java.util.function.LongToDoubleFunction after) {
		return ToDoubleBiFunction.of(this, after);
	}

	@Override
	default ToIntBiFunction<V1, V2> thenApplyAsInt(java.util.function.LongToIntFunction after) {
		return ToIntBiFunction.of(this, after);
	}

	@Override
	default ToLongBiFunction<V1, V2> thenApplyAsLong(java.util.function.LongUnaryOperator after) {
		return ToLongBiFunction.of(this, after);
	}

	@Override
	default BiFunction<V1, V2, Long> thenBox() {
		return this.thenApply(LongUnaryOperator.identity().thenBox());
	}

	@Override
	default BiPredicate<V1, V2> thenTest(java.util.function.LongPredicate after) {
		return BiPredicate.of(this, after);
	}

	/**
	 * Returns a function returning the specified value, ignoring its parameters.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param value the return value
	 * @return a function returning the specified value, ignoring its parameters.
	 */
	static <T1, T2> ToLongBiFunction<T1, T2> of(long value) {
		return new ToLongBiFunction<>() {
			@Override
			public long applyAsLong(T1 value1, T2 value2) {
				return value;
			}

			@Override
			public BiFunction<T1, T2, Long> thenBox() {
				return BiFunction.of(Long.valueOf(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T1, T2> ToLongBiFunction<T1, T2> of(java.util.function.BiConsumer<? super T1, ? super T2> before, java.util.function.LongSupplier after) {
		return new ToLongBiFunction<>() {
			@Override
			public long applyAsLong(T1 value1, T2 value2) {
				before.accept(value1, value2);
				return after.getAsLong();
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param <V> the intermediate type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T1, T2, V> ToLongBiFunction<T1, T2> of(java.util.function.BiFunction<? super T1, ? super T2, ? extends V> before, java.util.function.ToLongFunction<? super V> after) {
		return new ToLongBiFunction<>() {
			@Override
			public long applyAsLong(T1 value1, T2 value2) {
				return after.applyAsLong(before.apply(value1, value2));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T1, T2> ToLongBiFunction<T1, T2> of(java.util.function.BiPredicate<? super T1, ? super T2> before, BooleanToLongFunction after) {
		return new ToLongBiFunction<>() {
			@Override
			public long applyAsLong(T1 value1, T2 value2) {
				return after.applyAsLong(before.test(value1, value2));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T1, T2> ToLongBiFunction<T1, T2> of(java.util.function.ToDoubleBiFunction<? super T1, ? super T2> before, java.util.function.DoubleToLongFunction after) {
		return new ToLongBiFunction<>() {
			@Override
			public long applyAsLong(T1 value1, T2 value2) {
				return after.applyAsLong(before.applyAsDouble(value1, value2));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T1, T2> ToLongBiFunction<T1, T2> of(java.util.function.ToIntBiFunction<? super T1, ? super T2> before, java.util.function.IntToLongFunction after) {
		return new ToLongBiFunction<>() {
			@Override
			public long applyAsLong(T1 value1, T2 value2) {
				return after.applyAsLong(before.applyAsInt(value1, value2));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T1, T2> ToLongBiFunction<T1, T2> of(java.util.function.ToLongBiFunction<? super T1, ? super T2> before, java.util.function.LongUnaryOperator after) {
		return new ToLongBiFunction<>() {
			@Override
			public long applyAsLong(T1 value1, T2 value2) {
				return after.applyAsLong(before.applyAsLong(value1, value2));
			}
		};
	}

	/**
	 * Returns a function that delegates to one of two functions based on the specified predicate.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param predicate a predicate
	 * @param accepted the function to apply when accepted by the specified predicate
	 * @param rejected the function to apply when rejected by the specified predicate
	 * @return a function that delegates to one of two functions based on the specified predicate.
	 */
	static <T1, T2> ToLongBiFunction<T1, T2> when(java.util.function.BiPredicate<? super T1, ? super T2> predicate, java.util.function.ToLongBiFunction<? super T1, ? super T2> accepted, java.util.function.ToLongBiFunction<? super T1, ? super T2> rejected) {
		return new ToLongBiFunction<>() {
			@Override
			public long applyAsLong(T1 value1, T2 value2) {
				java.util.function.ToLongBiFunction<? super T1, ? super T2> function = predicate.test(value1, value2) ? accepted : rejected;
				return function.applyAsLong(value1, value2);
			}
		};
	}
}
