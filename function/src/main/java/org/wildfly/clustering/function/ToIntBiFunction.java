/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.Map;

/**
 * A binary function returning an int value.
 * @author Paul Ferraro
 * @param <V1> the former parameter type
 * @param <V2> the latter parameter type
 */
public interface ToIntBiFunction<V1, V2> extends java.util.function.ToIntBiFunction<V1, V2>, BinaryObjectOperation<V1, V2>, ToIntOperation {

	@Override
	default <T1, T2> ToIntBiFunction<T1, T2> compose(java.util.function.Function<? super T1, ? extends V1> beforeFormer, java.util.function.Function<? super T2, ? extends V2> beforeLatter) {
		return new ToIntBiFunction<>() {
			@Override
			public int applyAsInt(T1 value1, T2 value2) {
				return ToIntBiFunction.this.applyAsInt(beforeFormer.apply(value1), beforeLatter.apply(value2));
			}
		};
	}

	@Override
	default ToIntFunction<Map.Entry<V1, V2>> composeEntry() {
		return this.composeUnary(Map.Entry::getKey, Map.Entry::getValue);
	}

	@Override
	default <T> ToIntFunction<T> composeUnary(java.util.function.Function<? super T, ? extends V1> beforeFormer, java.util.function.Function<? super T, ? extends V2> beforeLatter) {
		return new ToIntFunction<>() {
			@Override
			public int applyAsInt(T value) {
				return ToIntBiFunction.this.applyAsInt(beforeFormer.apply(value), beforeLatter.apply(value));
			}
		};
	}

	@Override
	default ToIntBiFunction<V2, V1> reverse() {
		return new ToIntBiFunction<>() {
			@Override
			public int applyAsInt(V2 value2, V1 value1) {
				return ToIntBiFunction.this.applyAsInt(value1, value2);
			}
		};
	}

	@Override
	default BiConsumer<V1, V2> thenAccept(java.util.function.IntConsumer after) {
		return BiConsumer.of(this, after);
	}

	@Override
	default <R> BiFunction<V1, V2, R> thenApply(java.util.function.IntFunction<? extends R> after) {
		return BiFunction.of(this, after);
	}

	@Override
	default ToDoubleBiFunction<V1, V2> thenApplyAsDouble(java.util.function.IntToDoubleFunction after) {
		return ToDoubleBiFunction.of(this, after);
	}

	@Override
	default ToIntBiFunction<V1, V2> thenApplyAsInt(java.util.function.IntUnaryOperator after) {
		return ToIntBiFunction.of(this, after);
	}

	@Override
	default ToLongBiFunction<V1, V2> thenApplyAsLong(java.util.function.IntToLongFunction after) {
		return ToLongBiFunction.of(this, after);
	}

	@Override
	default BiFunction<V1, V2, Integer> thenBox() {
		return this.thenApply(IntUnaryOperator.identity().thenBox());
	}

	@Override
	default BiPredicate<V1, V2> thenTest(java.util.function.IntPredicate after) {
		return BiPredicate.of(this, after);
	}

	/**
	 * Returns a function returning the specified value, ignoring its parameters.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param value the return value
	 * @return a function returning the specified value, ignoring its parameters.
	 */
	static <T1, T2> ToIntBiFunction<T1, T2> of(int value) {
		return new ToIntBiFunction<>() {
			@Override
			public int applyAsInt(T1 value1, T2 value2) {
				return value;
			}

			@Override
			public BiFunction<T1, T2, Integer> thenBox() {
				return BiFunction.of(Integer.valueOf(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function.
	 */
	static <T1, T2> ToIntBiFunction<T1, T2> of(java.util.function.BiConsumer<? super T1, ? super T2> before, java.util.function.IntSupplier after) {
		return new ToIntBiFunction<>() {
			@Override
			public int applyAsInt(T1 value1, T2 value2) {
				before.accept(value1, value2);
				return after.getAsInt();
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
	 * @return a composite function.
	 */
	static <T1, T2, V> ToIntBiFunction<T1, T2> of(java.util.function.BiFunction<? super T1, ? super T2, ? extends V> before, java.util.function.ToIntFunction<? super V> after) {
		return new ToIntBiFunction<>() {
			@Override
			public int applyAsInt(T1 value1, T2 value2) {
				return after.applyAsInt(before.apply(value1, value2));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function.
	 */
	static <T1, T2> ToIntBiFunction<T1, T2> of(java.util.function.BiPredicate<? super T1, ? super T2> before, BooleanToIntFunction after) {
		return new ToIntBiFunction<>() {
			@Override
			public int applyAsInt(T1 value1, T2 value2) {
				return after.applyAsInt(before.test(value1, value2));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function.
	 */
	static <T1, T2> ToIntBiFunction<T1, T2> of(java.util.function.ToDoubleBiFunction<? super T1, ? super T2> before, java.util.function.DoubleToIntFunction after) {
		return new ToIntBiFunction<>() {
			@Override
			public int applyAsInt(T1 value1, T2 value2) {
				return after.applyAsInt(before.applyAsDouble(value1, value2));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function.
	 */
	static <T1, T2> ToIntBiFunction<T1, T2> of(java.util.function.ToIntBiFunction<? super T1, ? super T2> before, java.util.function.IntUnaryOperator after) {
		return new ToIntBiFunction<>() {
			@Override
			public int applyAsInt(T1 value1, T2 value2) {
				return after.applyAsInt(before.applyAsInt(value1, value2));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function.
	 */
	static <T1, T2> ToIntBiFunction<T1, T2> of(java.util.function.ToLongBiFunction<? super T1, ? super T2> before, java.util.function.LongToIntFunction after) {
		return new ToIntBiFunction<>() {
			@Override
			public int applyAsInt(T1 value1, T2 value2) {
				return after.applyAsInt(before.applyAsLong(value1, value2));
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
	static <T1, T2> ToIntBiFunction<T1, T2> when(java.util.function.BiPredicate<? super T1, ? super T2> predicate, java.util.function.ToIntBiFunction<? super T1, ? super T2> accepted, java.util.function.ToIntBiFunction<? super T1, ? super T2> rejected) {
		return new ToIntBiFunction<>() {
			@Override
			public int applyAsInt(T1 value1, T2 value2) {
				java.util.function.ToIntBiFunction<? super T1, ? super T2> function = predicate.test(value1, value2) ? accepted : rejected;
				return function.applyAsInt(value1, value2);
			}
		};
	}
}
