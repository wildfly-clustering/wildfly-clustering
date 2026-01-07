/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function operating on a boolean value, returning a double value.
 * @author Paul Ferraro
 */
public interface BooleanToDoubleFunction extends BooleanOperation, ToDoubleOperation {
	/**
	 * Applies the specified value.
	 * @param value the function parameter
	 * @return the function result
	 */
	double applyAsDouble(boolean value);

	@Override
	default ToDoubleFunction<Boolean> box() {
		return this.compose(BooleanPredicate.identity().box());
	}

	@Override
	default <V> ToDoubleFunction<V> compose(java.util.function.Predicate<? super V> before) {
		return ToDoubleFunction.of(before, this);
	}

	@Override
	default <V1, V2> ToDoubleBiFunction<V1, V2> composeBinary(java.util.function.BiPredicate<? super V1, ? super V2> before) {
		return ToDoubleBiFunction.of(before, this);
	}

	@Override
	default BooleanToDoubleFunction composeBoolean(BooleanPredicate before) {
		return BooleanToDoubleFunction.of(before, this);
	}

	@Override
	default DoubleUnaryOperator composeDouble(java.util.function.DoublePredicate before) {
		return DoubleUnaryOperator.of(before, this);
	}

	@Override
	default IntToDoubleFunction composeInt(java.util.function.IntPredicate before) {
		return IntToDoubleFunction.of(before, this);
	}

	@Override
	default LongToDoubleFunction composeLong(java.util.function.LongPredicate before) {
		return LongToDoubleFunction.of(before, this);
	}

	@Override
	default BooleanConsumer thenAccept(java.util.function.DoubleConsumer after) {
		return BooleanConsumer.of(this, after);
	}

	@Override
	default <R> BooleanFunction<R> thenApply(java.util.function.DoubleFunction<? extends R> after) {
		return BooleanFunction.of(this, after);
	}

	@Override
	default BooleanToDoubleFunction thenApplyAsDouble(java.util.function.DoubleUnaryOperator after) {
		return BooleanToDoubleFunction.of(this, after);
	}

	@Override
	default BooleanToIntFunction thenApplyAsInt(java.util.function.DoubleToIntFunction after) {
		return BooleanToIntFunction.of(this, after);
	}

	@Override
	default BooleanToLongFunction thenApplyAsLong(java.util.function.DoubleToLongFunction after) {
		return BooleanToLongFunction.of(this, after);
	}

	@Override
	default BooleanFunction<Double> thenBox() {
		return this.thenApply(DoubleUnaryOperator.identity().thenBox());
	}

	@Override
	default BooleanPredicate thenTest(java.util.function.DoublePredicate after) {
		return BooleanPredicate.of(this, after);
	}

	/**
	 * Returns a function that returns a fixed value, ignoring its parameter.
	 * @param value the return value
	 * @return a function that returns a fixed value.
	 */
	static BooleanToDoubleFunction of(double value) {
		return new BooleanToDoubleFunction() {
			@Override
			public double applyAsDouble(boolean ignore) {
				return value;
			}

			@Override
			public ToDoubleFunction<Boolean> box() {
				return ToDoubleFunction.of(value);
			}

			@Override
			public BooleanFunction<Double> thenBox() {
				return BooleanFunction.of(Double.valueOf(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static BooleanToDoubleFunction of(BooleanConsumer before, java.util.function.DoubleSupplier after) {
		return new BooleanToDoubleFunction() {
			@Override
			public double applyAsDouble(boolean value) {
				before.accept(value);
				return after.getAsDouble();
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static BooleanToDoubleFunction of(BooleanPredicate before, BooleanToDoubleFunction after) {
		return new BooleanToDoubleFunction() {
			@Override
			public double applyAsDouble(boolean value) {
				return after.applyAsDouble(before.test(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <T> the intermediate type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <T> BooleanToDoubleFunction of(BooleanFunction<? extends T> before, java.util.function.ToDoubleFunction<? super T> after) {
		return new BooleanToDoubleFunction() {
			@Override
			public double applyAsDouble(boolean value) {
				return after.applyAsDouble(before.apply(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static BooleanToDoubleFunction of(BooleanToDoubleFunction before, java.util.function.DoubleUnaryOperator after) {
		return new BooleanToDoubleFunction() {
			@Override
			public double applyAsDouble(boolean value) {
				return after.applyAsDouble(before.applyAsDouble(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static BooleanToDoubleFunction of(BooleanToIntFunction before, java.util.function.IntToDoubleFunction after) {
		return new BooleanToDoubleFunction() {
			@Override
			public double applyAsDouble(boolean value) {
				return after.applyAsDouble(before.applyAsInt(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static BooleanToDoubleFunction of(BooleanToLongFunction before, java.util.function.LongToDoubleFunction after) {
		return new BooleanToDoubleFunction() {
			@Override
			public double applyAsDouble(boolean value) {
				return after.applyAsDouble(before.applyAsLong(value));
			}
		};
	}
}
