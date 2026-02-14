/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function operating on a boolean value, returning a int value.
 * @author Paul Ferraro
 */
public interface BooleanToIntFunction extends BooleanOperation, ToIntOperation {
	/**
	 * Applies the specified value.
	 * @param value the function parameter
	 * @return the function result
	 */
	int applyAsInt(boolean value);

	@Override
	default ToIntFunction<Boolean> box() {
		return this.compose(BooleanPredicate.identity().box());
	}

	@Override
	default <V> ToIntFunction<V> compose(java.util.function.Predicate<? super V> before) {
		return ToIntFunction.of(before, this);
	}

	@Override
	default <V1, V2> ToIntBiFunction<V1, V2> composeBinary(java.util.function.BiPredicate<? super V1, ? super V2> before) {
		return ToIntBiFunction.of(before, this);
	}

	@Override
	default BooleanToIntFunction composeBoolean(BooleanPredicate before) {
		return BooleanToIntFunction.of(before, this);
	}

	@Override
	default DoubleToIntFunction composeDouble(java.util.function.DoublePredicate before) {
		return DoubleToIntFunction.of(before, this);
	}

	@Override
	default IntUnaryOperator composeInt(java.util.function.IntPredicate before) {
		return IntUnaryOperator.of(before, this);
	}

	@Override
	default LongToIntFunction composeLong(java.util.function.LongPredicate before) {
		return LongToIntFunction.of(before, this);
	}

	@Override
	default BooleanConsumer thenAccept(java.util.function.IntConsumer after) {
		return BooleanConsumer.of(this, after);
	}

	@Override
	default <R> BooleanFunction<R> thenApply(java.util.function.IntFunction<? extends R> after) {
		return BooleanFunction.of(this, after);
	}

	@Override
	default BooleanToDoubleFunction thenApplyAsDouble(java.util.function.IntToDoubleFunction after) {
		return BooleanToDoubleFunction.of(this, after);
	}

	@Override
	default BooleanToIntFunction thenApplyAsInt(java.util.function.IntUnaryOperator after) {
		return BooleanToIntFunction.of(this, after);
	}

	@Override
	default BooleanToLongFunction thenApplyAsLong(java.util.function.IntToLongFunction after) {
		return BooleanToLongFunction.of(this, after);
	}

	@Override
	default BooleanFunction<Integer> thenBox() {
		return this.thenApply(IntUnaryOperator.identity().thenBox());
	}

	@Override
	default BooleanPredicate thenTest(java.util.function.IntPredicate after) {
		return BooleanPredicate.of(this, after);
	}

	/**
	 * Returns a function that returns a fixed value, ignoring its parameter.
	 * @param value the return value
	 * @return a function that returns a fixed value.
	 */
	static BooleanToIntFunction of(int value) {
		return new BooleanToIntFunction() {
			@Override
			public int applyAsInt(boolean ignore) {
				return value;
			}

			@Override
			public ToIntFunction<Boolean> box() {
				return ToIntFunction.of(value);
			}

			@Override
			public BooleanFunction<Integer> thenBox() {
				return BooleanFunction.of(Integer.valueOf(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static BooleanToIntFunction of(BooleanConsumer before, java.util.function.IntSupplier after) {
		return new BooleanToIntFunction() {
			@Override
			public int applyAsInt(boolean value) {
				before.accept(value);
				return after.getAsInt();
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static BooleanToIntFunction of(BooleanPredicate before, BooleanToIntFunction after) {
		return new BooleanToIntFunction() {
			@Override
			public int applyAsInt(boolean value) {
				return after.applyAsInt(before.test(value));
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
	static <T> BooleanToIntFunction of(BooleanFunction<? extends T> before, java.util.function.ToIntFunction<? super T> after) {
		return new BooleanToIntFunction() {
			@Override
			public int applyAsInt(boolean value) {
				return after.applyAsInt(before.apply(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static BooleanToIntFunction of(BooleanToDoubleFunction before, java.util.function.DoubleToIntFunction after) {
		return new BooleanToIntFunction() {
			@Override
			public int applyAsInt(boolean value) {
				return after.applyAsInt(before.applyAsDouble(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static BooleanToIntFunction of(BooleanToIntFunction before, java.util.function.IntUnaryOperator after) {
		return new BooleanToIntFunction() {
			@Override
			public int applyAsInt(boolean value) {
				return after.applyAsInt(before.applyAsInt(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static BooleanToIntFunction of(BooleanToLongFunction before, java.util.function.LongToIntFunction after) {
		return new BooleanToIntFunction() {
			@Override
			public int applyAsInt(boolean value) {
				return after.applyAsInt(before.applyAsLong(value));
			}
		};
	}
}
