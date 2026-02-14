/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function operating on an int value, returning a long value.
 * @author Paul Ferraro
 */
public interface IntToLongFunction extends java.util.function.IntToLongFunction, IntOperation, ToLongOperation {

	@Override
	default ToLongFunction<Integer> box() {
		return this.compose(IntUnaryOperator.identity().box());
	}

	@Override
	default <T> ToLongFunction<T> compose(java.util.function.ToIntFunction<? super T> before) {
		return ToLongFunction.of(before, this);
	}

	@Override
	default <T1, T2> ToLongBiFunction<T1, T2> composeBinary(java.util.function.ToIntBiFunction<? super T1, ? super T2> before) {
		return ToLongBiFunction.of(before, this);
	}

	@Override
	default BooleanToLongFunction composeBoolean(BooleanToIntFunction before) {
		return BooleanToLongFunction.of(before, this);
	}

	@Override
	default DoubleToLongFunction composeDouble(java.util.function.DoubleToIntFunction before) {
		return DoubleToLongFunction.of(before, this);
	}

	@Override
	default IntToLongFunction composeInt(java.util.function.IntUnaryOperator before) {
		return IntToLongFunction.of(before, this);
	}

	@Override
	default LongUnaryOperator composeLong(java.util.function.LongToIntFunction before) {
		return LongUnaryOperator.of(before, this);
	}

	@Override
	default IntFunction<Long> thenBox() {
		return this.thenApply(LongUnaryOperator.identity().thenBox());
	}

	@Override
	default IntConsumer thenAccept(java.util.function.LongConsumer after) {
		return IntConsumer.of(this, after);
	}

	@Override
	default <R> IntFunction<R> thenApply(java.util.function.LongFunction<? extends R> after) {
		return IntFunction.of(this, after);
	}

	@Override
	default IntToDoubleFunction thenApplyAsDouble(java.util.function.LongToDoubleFunction after) {
		return IntToDoubleFunction.of(this, after);
	}

	@Override
	default IntUnaryOperator thenApplyAsInt(java.util.function.LongToIntFunction after) {
		return IntUnaryOperator.of(this, after);
	}

	@Override
	default IntToLongFunction thenApplyAsLong(java.util.function.LongUnaryOperator after) {
		return IntToLongFunction.of(this, after);
	}

	@Override
	default IntPredicate thenTest(java.util.function.LongPredicate after) {
		return IntPredicate.of(this, after);
	}

	/**
	 * Returns a function that returns its widened parameter.
	 * @return a function that returns its widened parameter.
	 */
	static IntToLongFunction identity() {
		return IdentityIntToLongFunction.INSTANCE;
	}

	/**
	 * Returns a function that returns a fixed value, ignoring its parameter.
	 * @param value the return value
	 * @return a function that returns a fixed value.
	 */
	static IntToLongFunction of(long value) {
		return new IntToLongFunction() {
			@Override
			public long applyAsLong(int ignore) {
				return value;
			}

			@Override
			public ToLongFunction<Integer> box() {
				return ToLongFunction.of(value);
			}

			@Override
			public IntFunction<Long> thenBox() {
				return IntFunction.of(Long.valueOf(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static IntToLongFunction of(java.util.function.IntConsumer before, java.util.function.LongSupplier after) {
		return new IntToLongFunction() {
			@Override
			public long applyAsLong(int value) {
				before.accept(value);
				return after.getAsLong();
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
	static <T> IntToLongFunction of(java.util.function.IntFunction<? extends T> before, java.util.function.ToLongFunction<? super T> after) {
		return new IntToLongFunction() {
			@Override
			public long applyAsLong(int value) {
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
	static IntToLongFunction of(java.util.function.IntPredicate before, BooleanToLongFunction after) {
		return new IntToLongFunction() {
			@Override
			public long applyAsLong(int value) {
				return after.applyAsLong(before.test(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static IntToLongFunction of(java.util.function.IntToDoubleFunction before, java.util.function.DoubleToLongFunction after) {
		return new IntToLongFunction() {
			@Override
			public long applyAsLong(int value) {
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
	static IntToLongFunction of(java.util.function.IntUnaryOperator before, java.util.function.IntToLongFunction after) {
		return new IntToLongFunction() {
			@Override
			public long applyAsLong(int value) {
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
	static IntToLongFunction of(java.util.function.IntToLongFunction before, java.util.function.LongUnaryOperator after) {
		return new IntToLongFunction() {
			@Override
			public long applyAsLong(int value) {
				return after.applyAsLong(before.applyAsLong(value));
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
	static IntToLongFunction when(java.util.function.IntPredicate predicate, java.util.function.IntToLongFunction accepted, java.util.function.IntToLongFunction rejected) {
		return new IntToLongFunction() {
			@Override
			public long applyAsLong(int value) {
				java.util.function.IntToLongFunction function = predicate.test(value) ? accepted : rejected;
				return function.applyAsLong(value);
			}
		};
	}

	/**
	 * A function that returns its widened parameter.
	 */
	class IdentityIntToLongFunction implements IntToLongFunction {
		static final IntToLongFunction INSTANCE = new IdentityIntToLongFunction();

		private IdentityIntToLongFunction() {
			// Hide
		}

		@Override
		public long applyAsLong(int value) {
			return value;
		}
	}
}
