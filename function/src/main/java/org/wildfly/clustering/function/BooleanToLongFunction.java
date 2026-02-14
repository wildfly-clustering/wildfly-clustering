/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function operating on a boolean value, returning a long value.
 * @author Paul Ferraro
 */
public interface BooleanToLongFunction extends BooleanOperation, ToLongOperation {
	/**
	 * Applies the specified value.
	 * @param value the function parameter
	 * @return the function result
	 */
	long applyAsLong(boolean value);

	@Override
	default ToLongFunction<Boolean> box() {
		return this.compose(BooleanPredicate.identity().box());
	}

	@Override
	default <V> ToLongFunction<V> compose(java.util.function.Predicate<? super V> before) {
		return ToLongFunction.of(before, this);
	}

	@Override
	default <V1, V2> ToLongBiFunction<V1, V2> composeBinary(java.util.function.BiPredicate<? super V1, ? super V2> before) {
		return ToLongBiFunction.of(before, this);
	}

	@Override
	default BooleanToLongFunction composeBoolean(BooleanPredicate before) {
		return BooleanToLongFunction.of(before, this);
	}

	@Override
	default DoubleToLongFunction composeDouble(java.util.function.DoublePredicate before) {
		return DoubleToLongFunction.of(before, this);
	}

	@Override
	default IntToLongFunction composeInt(java.util.function.IntPredicate before) {
		return IntToLongFunction.of(before, this);
	}

	@Override
	default LongUnaryOperator composeLong(java.util.function.LongPredicate before) {
		return LongUnaryOperator.of(before, this);
	}

	@Override
	default BooleanConsumer thenAccept(java.util.function.LongConsumer after) {
		return BooleanConsumer.of(this, after);
	}

	@Override
	default <R> BooleanFunction<R> thenApply(java.util.function.LongFunction<? extends R> after) {
		return BooleanFunction.of(this, after);
	}

	@Override
	default BooleanToDoubleFunction thenApplyAsDouble(java.util.function.LongToDoubleFunction after) {
		return BooleanToDoubleFunction.of(this, after);
	}

	@Override
	default BooleanToIntFunction thenApplyAsInt(java.util.function.LongToIntFunction after) {
		return BooleanToIntFunction.of(this, after);
	}

	@Override
	default BooleanToLongFunction thenApplyAsLong(java.util.function.LongUnaryOperator after) {
		return BooleanToLongFunction.of(this, after);
	}

	@Override
	default BooleanFunction<Long> thenBox() {
		return this.thenApply(LongUnaryOperator.identity().thenBox());
	}

	@Override
	default BooleanPredicate thenTest(java.util.function.LongPredicate after) {
		return BooleanPredicate.of(this, after);
	}

	/**
	 * Returns a function that returns a fixed value, ignoring its parameter.
	 * @param value the return value
	 * @return a function that returns a fixed value.
	 */
	static BooleanToLongFunction of(long value) {
		return new BooleanToLongFunction() {
			@Override
			public long applyAsLong(boolean ignore) {
				return value;
			}

			@Override
			public ToLongFunction<Boolean> box() {
				return ToLongFunction.of(value);
			}

			@Override
			public BooleanFunction<Long> thenBox() {
				return BooleanFunction.of(Long.valueOf(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static BooleanToLongFunction of(BooleanConsumer before, java.util.function.LongSupplier after) {
		return new BooleanToLongFunction() {
			@Override
			public long applyAsLong(boolean value) {
				before.accept(value);
				return after.getAsLong();
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static BooleanToLongFunction of(BooleanPredicate before, BooleanToLongFunction after) {
		return new BooleanToLongFunction() {
			@Override
			public long applyAsLong(boolean value) {
				return after.applyAsLong(before.test(value));
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
	static <T> BooleanToLongFunction of(BooleanFunction<? extends T> before, java.util.function.ToLongFunction<? super T> after) {
		return new BooleanToLongFunction() {
			@Override
			public long applyAsLong(boolean value) {
				return after.applyAsLong(before.apply(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static BooleanToLongFunction of(BooleanToDoubleFunction before, java.util.function.DoubleToLongFunction after) {
		return new BooleanToLongFunction() {
			@Override
			public long applyAsLong(boolean value) {
				return after.applyAsLong(before.applyAsDouble(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static BooleanToLongFunction of(BooleanToIntFunction before, java.util.function.IntToLongFunction after) {
		return new BooleanToLongFunction() {
			@Override
			public long applyAsLong(boolean value) {
				return after.applyAsLong(before.applyAsInt(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static BooleanToLongFunction of(BooleanToLongFunction before, java.util.function.LongUnaryOperator after) {
		return new BooleanToLongFunction() {
			@Override
			public long applyAsLong(boolean value) {
				return after.applyAsLong(before.applyAsLong(value));
			}
		};
	}
}
