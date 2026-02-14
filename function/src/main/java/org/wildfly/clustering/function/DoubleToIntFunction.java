/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function operating on a double value, returning an int value.
 * @author Paul Ferraro
 */
public interface DoubleToIntFunction extends java.util.function.DoubleToIntFunction, DoubleOperation, ToIntOperation {

	@Override
	default ToIntFunction<Double> box() {
		return this.compose(DoubleUnaryOperator.identity().box());
	}

	@Override
	default <T> ToIntFunction<T> compose(java.util.function.ToDoubleFunction<? super T> before) {
		return ToIntFunction.of(before, this);
	}

	@Override
	default <T1, T2> ToIntBiFunction<T1, T2> composeBinary(java.util.function.ToDoubleBiFunction<? super T1, ? super T2> before) {
		return ToIntBiFunction.of(before, this);
	}

	@Override
	default BooleanToIntFunction composeBoolean(BooleanToDoubleFunction before) {
		return BooleanToIntFunction.of(before, this);
	}

	@Override
	default DoubleToIntFunction composeDouble(java.util.function.DoubleUnaryOperator before) {
		return DoubleToIntFunction.of(before, this);
	}

	@Override
	default IntUnaryOperator composeInt(java.util.function.IntToDoubleFunction before) {
		return IntUnaryOperator.of(before, this);
	}

	@Override
	default LongToIntFunction composeLong(java.util.function.LongToDoubleFunction before) {
		return LongToIntFunction.of(before, this);
	}

	@Override
	default DoubleConsumer thenAccept(java.util.function.IntConsumer after) {
		return DoubleConsumer.of(this, after);
	}

	@Override
	default <R> DoubleFunction<R> thenApply(java.util.function.IntFunction<? extends R> after) {
		return DoubleFunction.of(this, after);
	}

	@Override
	default DoubleUnaryOperator thenApplyAsDouble(java.util.function.IntToDoubleFunction after) {
		return DoubleUnaryOperator.of(this, after);
	}

	@Override
	default DoubleToIntFunction thenApplyAsInt(java.util.function.IntUnaryOperator after) {
		return DoubleToIntFunction.of(this, after);
	}

	@Override
	default DoubleToLongFunction thenApplyAsLong(java.util.function.IntToLongFunction after) {
		return DoubleToLongFunction.of(this, after);
	}

	@Override
	default DoubleFunction<Integer> thenBox() {
		return this.thenApply(IntUnaryOperator.identity().thenBox());
	}

	@Override
	default DoublePredicate thenTest(java.util.function.IntPredicate after) {
		return DoublePredicate.of(this, after);
	}

	/**
	 * Returns a function that returns its narrowed parameter.
	 * @return a function that returns its narrowed parameter.
	 */
	static DoubleToIntFunction identity() {
		return IdentityDoubleToIntFunction.INSTANCE;
	}

	/**
	 * Returns a function that returns a fixed value, ignoring its parameter.
	 * @param value the return value
	 * @return a function that returns a fixed value.
	 */
	static DoubleToIntFunction of(int value) {
		return new DoubleToIntFunction() {
			@Override
			public int applyAsInt(double ignore) {
				return value;
			}

			@Override
			public ToIntFunction<Double> box() {
				return ToIntFunction.of(value);
			}

			@Override
			public DoubleFunction<Integer> thenBox() {
				return DoubleFunction.of(Integer.valueOf(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static DoubleToIntFunction of(java.util.function.DoubleConsumer before, java.util.function.IntSupplier after) {
		return new DoubleToIntFunction() {
			@Override
			public int applyAsInt(double value) {
				before.accept(value);
				return after.getAsInt();
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
	static <T> DoubleToIntFunction of(java.util.function.DoubleFunction<? extends T> before, java.util.function.ToIntFunction<? super T> after) {
		return new DoubleToIntFunction() {
			@Override
			public int applyAsInt(double value) {
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
	static DoubleToIntFunction of(java.util.function.DoublePredicate before, BooleanToIntFunction after) {
		return new DoubleToIntFunction() {
			@Override
			public int applyAsInt(double value) {
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
	static DoubleToIntFunction of(java.util.function.DoubleUnaryOperator before, java.util.function.DoubleToIntFunction after) {
		return new DoubleToIntFunction() {
			@Override
			public int applyAsInt(double value) {
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
	static DoubleToIntFunction of(java.util.function.DoubleToIntFunction before, java.util.function.IntUnaryOperator after) {
		return new DoubleToIntFunction() {
			@Override
			public int applyAsInt(double value) {
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
	static DoubleToIntFunction of(java.util.function.DoubleToLongFunction before, java.util.function.LongToIntFunction after) {
		return new DoubleToIntFunction() {
			@Override
			public int applyAsInt(double value) {
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
	static DoubleToIntFunction when(java.util.function.DoublePredicate predicate, java.util.function.DoubleToIntFunction accepted, java.util.function.DoubleToIntFunction rejected) {
		return new DoubleToIntFunction() {
			@Override
			public int applyAsInt(double value) {
				java.util.function.DoubleToIntFunction function = predicate.test(value) ? accepted : rejected;
				return function.applyAsInt(value);
			}
		};
	}

	/**
	 * A function that returns its narrowed parameter.
	 */
	class IdentityDoubleToIntFunction implements DoubleToIntFunction {
		static final DoubleToIntFunction INSTANCE = new IdentityDoubleToIntFunction();

		private IdentityDoubleToIntFunction() {
			// Hide
		}

		@Override
		public int applyAsInt(double value) {
			return (int) value;
		}
	}
}
