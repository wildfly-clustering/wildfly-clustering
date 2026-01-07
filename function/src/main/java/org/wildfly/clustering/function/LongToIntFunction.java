/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function operating on a long value, returning an int value.
 * @author Paul Ferraro
 */
public interface LongToIntFunction extends java.util.function.LongToIntFunction, LongOperation, ToIntOperation {

	@Override
	default ToIntFunction<Long> box() {
		return this.compose(LongUnaryOperator.identity().box());
	}

	@Override
	default <T> ToIntFunction<T> compose(java.util.function.ToLongFunction<? super T> before) {
		return ToIntFunction.of(before, this);
	}

	@Override
	default <T1, T2> ToIntBiFunction<T1, T2> composeBinary(java.util.function.ToLongBiFunction<? super T1, ? super T2> before) {
		return ToIntBiFunction.of(before, this);
	}

	@Override
	default BooleanToIntFunction composeBoolean(BooleanToLongFunction before) {
		return BooleanToIntFunction.of(before, this);
	}

	@Override
	default DoubleToIntFunction composeDouble(java.util.function.DoubleToLongFunction before) {
		return DoubleToIntFunction.of(before, this);
	}

	@Override
	default IntUnaryOperator composeInt(java.util.function.IntToLongFunction before) {
		return IntUnaryOperator.of(before, this);
	}

	@Override
	default LongToIntFunction composeLong(java.util.function.LongUnaryOperator before) {
		return LongToIntFunction.of(before, this);
	}

	@Override
	default LongConsumer thenAccept(java.util.function.IntConsumer after) {
		return LongConsumer.of(this, after);
	}

	@Override
	default <R> LongFunction<R> thenApply(java.util.function.IntFunction<? extends R> after) {
		return LongFunction.of(this, after);
	}

	@Override
	default LongToDoubleFunction thenApplyAsDouble(java.util.function.IntToDoubleFunction after) {
		return LongToDoubleFunction.of(this, after);
	}

	@Override
	default LongToIntFunction thenApplyAsInt(java.util.function.IntUnaryOperator after) {
		return LongToIntFunction.of(this, after);
	}

	@Override
	default LongUnaryOperator thenApplyAsLong(java.util.function.IntToLongFunction after) {
		return LongUnaryOperator.of(this, after);
	}

	@Override
	default LongFunction<Integer> thenBox() {
		return this.thenApply(IntUnaryOperator.identity().thenBox());
	}

	@Override
	default LongPredicate thenTest(java.util.function.IntPredicate after) {
		return LongPredicate.of(this, after);
	}

	/**
	 * Returns a function that always returns its narrowed parameter.
	 * @return a function that always returns its narrowed parameter.
	 */
	static LongToIntFunction identity() {
		return IdentityLongToIntFunction.INSTANCE;
	}

	/**
	 * Returns a function that returns a fixed value, ignoring its parameter.
	 * @param value the return value
	 * @return a function that returns a fixed value.
	 */
	static LongToIntFunction of(int value) {
		return new LongToIntFunction() {
			@Override
			public int applyAsInt(long ignore) {
				return value;
			}

			@Override
			public ToIntFunction<Long> box() {
				return ToIntFunction.of(value);
			}

			@Override
			public LongFunction<Integer> thenBox() {
				return LongFunction.of(Integer.valueOf(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static LongToIntFunction of(java.util.function.LongConsumer before, java.util.function.IntSupplier after) {
		return new LongToIntFunction() {
			@Override
			public int applyAsInt(long value) {
				before.accept(value);
				return after.getAsInt();
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param <V> the intermediate type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static <V> LongToIntFunction of(java.util.function.LongFunction<? extends V> before, java.util.function.ToIntFunction<? super V> after) {
		return new LongToIntFunction() {
			@Override
			public int applyAsInt(long value) {
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
	static LongToIntFunction of(java.util.function.LongPredicate before, BooleanToIntFunction after) {
		return new LongToIntFunction() {
			@Override
			public int applyAsInt(long value) {
				return after.applyAsInt(before.test(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static LongToIntFunction of(java.util.function.LongToDoubleFunction before, java.util.function.DoubleToIntFunction after) {
		return new LongToIntFunction() {
			@Override
			public int applyAsInt(long value) {
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
	static LongToIntFunction of(java.util.function.LongToIntFunction before, java.util.function.IntUnaryOperator after) {
		return new LongToIntFunction() {
			@Override
			public int applyAsInt(long value) {
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
	static LongToIntFunction of(java.util.function.LongUnaryOperator before, java.util.function.LongToIntFunction after) {
		return new LongToIntFunction() {
			@Override
			public int applyAsInt(long value) {
				return after.applyAsInt(before.applyAsLong(value));
			}
		};
	}

	/**
	 * Returns a function that delegates to one of two functions based on the specified predicate.
	 * @param predicate a predicate
	 * @param accepted the function to apply when accepted by the specified predicate
	 * @param rejected the function to apply when rejected by the specified predicate
	 * @return a function that delegates to one of two functions based on the specified predicate.
	 */
	static LongToIntFunction when(java.util.function.LongPredicate predicate, java.util.function.LongToIntFunction accepted, java.util.function.LongToIntFunction rejected) {
		return new LongToIntFunction() {
			@Override
			public int applyAsInt(long value) {
				java.util.function.LongToIntFunction function = predicate.test(value) ? accepted : rejected;
				return function.applyAsInt(value);
			}
		};
	}

	/**
	 * A function returning its narrowed parameter.
	 */
	class IdentityLongToIntFunction implements LongToIntFunction {
		static final LongToIntFunction INSTANCE = new IdentityLongToIntFunction();

		private IdentityLongToIntFunction() {
			// Hide
		}

		@Override
		public int applyAsInt(long value) {
			return (int) value;
		}
	}
}
