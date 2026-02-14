/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function operating on a long value, returning a double value.
 * @author Paul Ferraro
 */
public interface LongToDoubleFunction extends java.util.function.LongToDoubleFunction, LongOperation, ToDoubleOperation {

	@Override
	default ToDoubleFunction<Long> box() {
		return this.compose(LongUnaryOperator.identity().box());
	}

	@Override
	default <T> ToDoubleFunction<T> compose(java.util.function.ToLongFunction<? super T> before) {
		return ToDoubleFunction.of(before, this);
	}

	@Override
	default <T1, T2> ToDoubleBiFunction<T1, T2> composeBinary(java.util.function.ToLongBiFunction<? super T1, ? super T2> before) {
		return ToDoubleBiFunction.of(before, this);
	}

	@Override
	default BooleanToDoubleFunction composeBoolean(BooleanToLongFunction before) {
		return BooleanToDoubleFunction.of(before, this);
	}

	@Override
	default DoubleUnaryOperator composeDouble(java.util.function.DoubleToLongFunction before) {
		return DoubleUnaryOperator.of(before, this);
	}

	@Override
	default IntToDoubleFunction composeInt(java.util.function.IntToLongFunction before) {
		return IntToDoubleFunction.of(before, this);
	}

	@Override
	default LongToDoubleFunction composeLong(java.util.function.LongUnaryOperator before) {
		return LongToDoubleFunction.of(before, this);
	}

	@Override
	default LongConsumer thenAccept(java.util.function.DoubleConsumer after) {
		return LongConsumer.of(this, after);
	}

	@Override
	default <R> LongFunction<R> thenApply(java.util.function.DoubleFunction<? extends R> after) {
		return LongFunction.of(this, after);
	}

	@Override
	default LongToDoubleFunction thenApplyAsDouble(java.util.function.DoubleUnaryOperator after) {
		return LongToDoubleFunction.of(this, after);
	}

	@Override
	default LongToIntFunction thenApplyAsInt(java.util.function.DoubleToIntFunction after) {
		return LongToIntFunction.of(this, after);
	}

	@Override
	default LongUnaryOperator thenApplyAsLong(java.util.function.DoubleToLongFunction after) {
		return LongUnaryOperator.of(this, after);
	}

	@Override
	default LongFunction<Double> thenBox() {
		return this.thenApply(DoubleUnaryOperator.identity().thenBox());
	}

	@Override
	default LongPredicate thenTest(java.util.function.DoublePredicate after) {
		return LongPredicate.of(this, after);
	}

	/**
	 * Returns a function that returns its widened parameter.
	 * @return a function that returns its widened parameter.
	 */
	static LongToDoubleFunction identity() {
		return IdentityLongToDoubleFunction.INSTANCE;
	}

	/**
	 * Returns a function that returns a fixed value, ignoring its parameter.
	 * @param value the return value
	 * @return a function that returns a fixed value.
	 */
	static LongToDoubleFunction of(double value) {
		return new LongToDoubleFunction() {
			@Override
			public double applyAsDouble(long ignore) {
				return value;
			}

			@Override
			public ToDoubleFunction<Long> box() {
				return ToDoubleFunction.of(value);
			}

			@Override
			public LongFunction<Double> thenBox() {
				return LongFunction.of(Double.valueOf(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static LongToDoubleFunction of(java.util.function.LongConsumer before, java.util.function.DoubleSupplier after) {
		return new LongToDoubleFunction() {
			@Override
			public double applyAsDouble(long value) {
				before.accept(value);
				return after.getAsDouble();
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
	static <V> LongToDoubleFunction of(java.util.function.LongFunction<? extends V> before, java.util.function.ToDoubleFunction<? super V> after) {
		return new LongToDoubleFunction() {
			@Override
			public double applyAsDouble(long value) {
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
	static LongToDoubleFunction of(java.util.function.LongPredicate before, BooleanToDoubleFunction after) {
		return new LongToDoubleFunction() {
			@Override
			public double applyAsDouble(long value) {
				return after.applyAsDouble(before.test(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static LongToDoubleFunction of(java.util.function.LongToDoubleFunction before, java.util.function.DoubleUnaryOperator after) {
		return new LongToDoubleFunction() {
			@Override
			public double applyAsDouble(long value) {
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
	static LongToDoubleFunction of(java.util.function.LongToIntFunction before, java.util.function.IntToDoubleFunction after) {
		return new LongToDoubleFunction() {
			@Override
			public double applyAsDouble(long value) {
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
	static LongToDoubleFunction of(java.util.function.LongUnaryOperator before, java.util.function.LongToDoubleFunction after) {
		return new LongToDoubleFunction() {
			@Override
			public double applyAsDouble(long value) {
				return after.applyAsDouble(before.applyAsLong(value));
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
	static LongToDoubleFunction when(java.util.function.LongPredicate predicate, java.util.function.LongToDoubleFunction accepted, java.util.function.LongToDoubleFunction rejected) {
		return new LongToDoubleFunction() {
			@Override
			public double applyAsDouble(long value) {
				java.util.function.LongToDoubleFunction function = predicate.test(value) ? accepted : rejected;
				return function.applyAsDouble(value);
			}
		};
	}

	/**
	 * A function returning its widened parameter.
	 */
	class IdentityLongToDoubleFunction implements LongToDoubleFunction {
		static final LongToDoubleFunction INSTANCE = new IdentityLongToDoubleFunction();

		private IdentityLongToDoubleFunction() {
			// Hide
		}

		@Override
		public double applyAsDouble(long value) {
			return value;
		}
	}
}
