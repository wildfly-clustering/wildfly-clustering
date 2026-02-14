/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.Map;

/**
 * A binary function returning a double value.
 * @author Paul Ferraro
 * @param <V1> the former parameter type
 * @param <V2> the latter parameter type
 */
public interface ToDoubleBiFunction<V1, V2> extends java.util.function.ToDoubleBiFunction<V1, V2>, BinaryObjectOperation<V1, V2>, ToDoubleOperation {

	@Override
	default <T1, T2> ToDoubleBiFunction<T1, T2> compose(java.util.function.Function<? super T1, ? extends V1> mapper1, java.util.function.Function<? super T2, ? extends V2> mapper2) {
		return new ToDoubleBiFunction<>() {
			@Override
			public double applyAsDouble(T1 value1, T2 value2) {
				return ToDoubleBiFunction.this.applyAsDouble(mapper1.apply(value1), mapper2.apply(value2));
			}
		};
	}

	@Override
	default ToDoubleFunction<Map.Entry<V1, V2>> composeEntry() {
		return this.composeUnary(Map.Entry::getKey, Map.Entry::getValue);
	}


	@Override
	default <T> ToDoubleFunction<T> composeUnary(java.util.function.Function<? super T, ? extends V1> mapper1, java.util.function.Function<? super T, ? extends V2> mapper2) {
		return new ToDoubleFunction<>() {
			@Override
			public double applyAsDouble(T value) {
				return ToDoubleBiFunction.this.applyAsDouble(mapper1.apply(value), mapper2.apply(value));
			}
		};
	}

	@Override
	default ToDoubleBiFunction<V2, V1> reverse() {
		return new ToDoubleBiFunction<>() {
			@Override
			public double applyAsDouble(V2 value2, V1 value1) {
				return ToDoubleBiFunction.this.applyAsDouble(value1, value2);
			}
		};
	}

	@Override
	default BiConsumer<V1, V2> thenAccept(java.util.function.DoubleConsumer after) {
		return BiConsumer.of(this, after);
	}

	@Override
	default <R> BiFunction<V1, V2, R> thenApply(java.util.function.DoubleFunction<? extends R> after) {
		return BiFunction.of(this, after);
	}

	@Override
	default ToDoubleBiFunction<V1, V2> thenApplyAsDouble(java.util.function.DoubleUnaryOperator after) {
		return ToDoubleBiFunction.of(this, after);
	}

	@Override
	default ToIntBiFunction<V1, V2> thenApplyAsInt(java.util.function.DoubleToIntFunction after) {
		return ToIntBiFunction.of(this, after);
	}

	@Override
	default ToLongBiFunction<V1, V2> thenApplyAsLong(java.util.function.DoubleToLongFunction after) {
		return ToLongBiFunction.of(this, after);
	}

	@Override
	default BiFunction<V1, V2, Double> thenBox() {
		return this.thenApply(DoubleUnaryOperator.identity().thenBox());
	}

	@Override
	default BiPredicate<V1, V2> thenTest(java.util.function.DoublePredicate after) {
		return BiPredicate.of(this, after);
	}

	/**
	 * Returns a function returning the specified value, ignoring its parameters.
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @param value the return value
	 * @return a function returning the specified value, ignoring its parameters.
	 */
	static <T1, T2> ToDoubleBiFunction<T1, T2> of(int value) {
		return new ToDoubleBiFunction<>() {
			@Override
			public double applyAsDouble(T1 value1, T2 value2) {
				return value;
			}

			@Override
			public BiFunction<T1, T2, Double> thenBox() {
				return BiFunction.of(Double.valueOf(value));
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
	static <T1, T2> ToDoubleBiFunction<T1, T2> of(java.util.function.BiConsumer<? super T1, ? super T2> before, java.util.function.DoubleSupplier after) {
		return new ToDoubleBiFunction<>() {
			@Override
			public double applyAsDouble(T1 value1, T2 value2) {
				before.accept(value1, value2);
				return after.getAsDouble();
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
	static <T1, T2, V> ToDoubleBiFunction<T1, T2> of(java.util.function.BiFunction<? super T1, ? super T2, ? extends V> before, java.util.function.ToDoubleFunction<? super V> after) {
		return new ToDoubleBiFunction<>() {
			@Override
			public double applyAsDouble(T1 value1, T2 value2) {
				return after.applyAsDouble(before.apply(value1, value2));
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
	static <T1, T2> ToDoubleBiFunction<T1, T2> of(java.util.function.BiPredicate<? super T1, ? super T2> before, BooleanToDoubleFunction after) {
		return new ToDoubleBiFunction<>() {
			@Override
			public double applyAsDouble(T1 value1, T2 value2) {
				return after.applyAsDouble(before.test(value1, value2));
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
	static <T1, T2> ToDoubleBiFunction<T1, T2> of(java.util.function.ToDoubleBiFunction<? super T1, ? super T2> before, java.util.function.DoubleUnaryOperator after) {
		return new ToDoubleBiFunction<>() {
			@Override
			public double applyAsDouble(T1 value1, T2 value2) {
				return after.applyAsDouble(before.applyAsDouble(value1, value2));
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
	static <T1, T2> ToDoubleBiFunction<T1, T2> of(java.util.function.ToIntBiFunction<? super T1, ? super T2> before, java.util.function.IntToDoubleFunction after) {
		return new ToDoubleBiFunction<>() {
			@Override
			public double applyAsDouble(T1 value1, T2 value2) {
				return after.applyAsDouble(before.applyAsInt(value1, value2));
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
	static <T1, T2> ToDoubleBiFunction<T1, T2> of(java.util.function.ToLongBiFunction<? super T1, ? super T2> before, java.util.function.LongToDoubleFunction after) {
		return new ToDoubleBiFunction<>() {
			@Override
			public double applyAsDouble(T1 value1, T2 value2) {
				return after.applyAsDouble(before.applyAsLong(value1, value2));
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
	static <T1, T2> ToDoubleBiFunction<T1, T2> when(java.util.function.BiPredicate<? super T1, ? super T2> predicate, java.util.function.ToDoubleBiFunction<? super T1, ? super T2> accepted, java.util.function.ToDoubleBiFunction<? super T1, ? super T2> rejected) {
		return new ToDoubleBiFunction<>() {
			@Override
			public double applyAsDouble(T1 value1, T2 value2) {
				java.util.function.ToDoubleBiFunction<? super T1, ? super T2> function = predicate.test(value1, value2) ? accepted : rejected;
				return function.applyAsDouble(value1, value2);
			}
		};
	}
}
