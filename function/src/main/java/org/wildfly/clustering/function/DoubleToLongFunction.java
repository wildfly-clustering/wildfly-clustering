/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function operating on a double value, returning a long value.
 * @author Paul Ferraro
 */
public interface DoubleToLongFunction extends java.util.function.DoubleToLongFunction, DoubleOperation, ToLongOperation {

	@Override
	default ToLongFunction<Double> box() {
		return this.compose(DoubleUnaryOperator.identity().box());
	}

	@Override
	default <T> ToLongFunction<T> compose(java.util.function.ToDoubleFunction<? super T> before) {
		return ToLongFunction.of(before, this);
	}

	@Override
	default <T1, T2> ToLongBiFunction<T1, T2> composeBinary(java.util.function.ToDoubleBiFunction<? super T1, ? super T2> before) {
		return ToLongBiFunction.of(before, this);
	}

	@Override
	default BooleanToLongFunction composeBoolean(BooleanToDoubleFunction before) {
		return BooleanToLongFunction.of(before, this);
	}

	@Override
	default DoubleToLongFunction composeDouble(java.util.function.DoubleUnaryOperator before) {
		return DoubleToLongFunction.of(before, this);
	}

	@Override
	default IntToLongFunction composeInt(java.util.function.IntToDoubleFunction before) {
		return IntToLongFunction.of(before, this);
	}

	@Override
	default LongUnaryOperator composeLong(java.util.function.LongToDoubleFunction before) {
		return LongUnaryOperator.of(before, this);
	}

	@Override
	default DoubleConsumer thenAccept(java.util.function.LongConsumer after) {
		return DoubleConsumer.of(this, after);
	}

	@Override
	default <R> DoubleFunction<R> thenApply(java.util.function.LongFunction<? extends R> after) {
		return DoubleFunction.of(this, after);
	}

	@Override
	default DoubleUnaryOperator thenApplyAsDouble(java.util.function.LongToDoubleFunction after) {
		return DoubleUnaryOperator.of(this, after);
	}

	@Override
	default DoubleToIntFunction thenApplyAsInt(java.util.function.LongToIntFunction after) {
		return DoubleToIntFunction.of(this, after);
	}

	@Override
	default DoubleToLongFunction thenApplyAsLong(java.util.function.LongUnaryOperator after) {
		return DoubleToLongFunction.of(this, after);
	}

	@Override
	default DoubleFunction<Long> thenBox() {
		return this.thenApply(LongUnaryOperator.identity().thenBox());
	}

	@Override
	default DoublePredicate thenTest(java.util.function.LongPredicate after) {
		return DoublePredicate.of(this, after);
	}

	/**
	 * Returns a function that returns its narrowed parameter.
	 * @return a function that returns its narrowed parameter.
	 */
	static DoubleToLongFunction identity() {
		return IdentityDoubleToLongFunction.INSTANCE;
	}

	/**
	 * Returns a function that returns a fixed value, ignoring its parameter.
	 * @param value the return value
	 * @return a function that returns a fixed value.
	 */
	static DoubleToLongFunction of(long value) {
		return new DoubleToLongFunction() {
			@Override
			public long applyAsLong(double ignore) {
				return value;
			}

			@Override
			public ToLongFunction<Double> box() {
				return ToLongFunction.of(value);
			}

			@Override
			public DoubleFunction<Long> thenBox() {
				return DoubleFunction.of(Long.valueOf(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static DoubleToLongFunction of(java.util.function.DoubleConsumer before, java.util.function.LongSupplier after) {
		return new DoubleToLongFunction() {
			@Override
			public long applyAsLong(double value) {
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
	static <T> DoubleToLongFunction of(java.util.function.DoubleFunction<? extends T> before, java.util.function.ToLongFunction<? super T> after) {
		return new DoubleToLongFunction() {
			@Override
			public long applyAsLong(double value) {
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
	static DoubleToLongFunction of(java.util.function.DoublePredicate before, BooleanToLongFunction after) {
		return new DoubleToLongFunction() {
			@Override
			public long applyAsLong(double value) {
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
	static DoubleToLongFunction of(java.util.function.DoubleUnaryOperator before, java.util.function.DoubleToLongFunction after) {
		return new DoubleToLongFunction() {
			@Override
			public long applyAsLong(double value) {
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
	static DoubleToLongFunction of(java.util.function.DoubleToIntFunction before, java.util.function.IntToLongFunction after) {
		return new DoubleToLongFunction() {
			@Override
			public long applyAsLong(double value) {
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
	static DoubleToLongFunction of(java.util.function.DoubleToLongFunction before, java.util.function.LongUnaryOperator after) {
		return new DoubleToLongFunction() {
			@Override
			public long applyAsLong(double value) {
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
	static DoubleToLongFunction when(java.util.function.DoublePredicate predicate, java.util.function.DoubleToLongFunction accepted, java.util.function.DoubleToLongFunction rejected) {
		return new DoubleToLongFunction() {
			@Override
			public long applyAsLong(double value) {
				java.util.function.DoubleToLongFunction function = predicate.test(value) ? accepted : rejected;
				return function.applyAsLong(value);
			}
		};
	}

	/**
	 * A function that returns its narrowed parameter.
	 */
	class IdentityDoubleToLongFunction implements DoubleToLongFunction {
		static final DoubleToLongFunction INSTANCE = new IdentityDoubleToLongFunction();

		private IdentityDoubleToLongFunction() {
			// Hide
		}

		@Override
		public long applyAsLong(double value) {
			return (long) value;
		}
	}
}
