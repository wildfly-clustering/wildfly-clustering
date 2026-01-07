/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function operating on an int value, returning a double value.
 * @author Paul Ferraro
 */
public interface IntToDoubleFunction extends java.util.function.IntToDoubleFunction, IntOperation, ToDoubleOperation {

	@Override
	default ToDoubleFunction<Integer> box() {
		return this.compose(IntUnaryOperator.identity().box());
	}

	@Override
	default <T> ToDoubleFunction<T> compose(java.util.function.ToIntFunction<? super T> before) {
		return ToDoubleFunction.of(before, this);
	}

	@Override
	default <T1, T2> ToDoubleBiFunction<T1, T2> composeBinary(java.util.function.ToIntBiFunction<? super T1, ? super T2> before) {
		return ToDoubleBiFunction.of(before, this);
	}

	@Override
	default BooleanToDoubleFunction composeBoolean(BooleanToIntFunction before) {
		return BooleanToDoubleFunction.of(before, this);
	}

	@Override
	default DoubleUnaryOperator composeDouble(java.util.function.DoubleToIntFunction before) {
		return DoubleUnaryOperator.of(before, this);
	}

	@Override
	default IntToDoubleFunction composeInt(java.util.function.IntUnaryOperator before) {
		return IntToDoubleFunction.of(before, this);
	}

	@Override
	default LongToDoubleFunction composeLong(java.util.function.LongToIntFunction before) {
		return LongToDoubleFunction.of(before, this);
	}

	@Override
	default IntConsumer thenAccept(java.util.function.DoubleConsumer after) {
		return IntConsumer.of(this, after);
	}

	@Override
	default <R> IntFunction<R> thenApply(java.util.function.DoubleFunction<? extends R> after) {
		return IntFunction.of(this, after);
	}

	@Override
	default IntToDoubleFunction thenApplyAsDouble(java.util.function.DoubleUnaryOperator after) {
		return IntToDoubleFunction.of(this, after);
	}

	@Override
	default IntUnaryOperator thenApplyAsInt(java.util.function.DoubleToIntFunction after) {
		return IntUnaryOperator.of(this, after);
	}

	@Override
	default IntToLongFunction thenApplyAsLong(java.util.function.DoubleToLongFunction after) {
		return IntToLongFunction.of(this, after);
	}

	@Override
	default IntFunction<Double> thenBox() {
		return this.thenApply(DoubleUnaryOperator.identity().thenBox());
	}

	@Override
	default IntPredicate thenTest(java.util.function.DoublePredicate after) {
		return IntPredicate.of(this, after);
	}

	/**
	 * Returns a function that always returns its widened parameter.
	 * @return a function that always returns its widened parameter.
	 */
	static IntToDoubleFunction identity() {
		return IdentityIntToDoubleFunction.INSTANCE;
	}

	/**
	 * Returns a function that returns a fixed value, ignoring its parameter.
	 * @param value the return value
	 * @return a function that returns a fixed value.
	 */
	static IntToDoubleFunction of(double value) {
		return new IntToDoubleFunction() {
			@Override
			public double applyAsDouble(int ignore) {
				return value;
			}

			@Override
			public ToDoubleFunction<Integer> box() {
				return ToDoubleFunction.of(value);
			}

			@Override
			public IntFunction<Double> thenBox() {
				return IntFunction.of(Double.valueOf(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static IntToDoubleFunction of(java.util.function.IntConsumer before, java.util.function.DoubleSupplier after) {
		return new IntToDoubleFunction() {
			@Override
			public double applyAsDouble(int value) {
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
	static <V> IntToDoubleFunction of(java.util.function.IntFunction<? extends V> before, java.util.function.ToDoubleFunction<? super V> after) {
		return new IntToDoubleFunction() {
			@Override
			public double applyAsDouble(int value) {
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
	static IntToDoubleFunction of(java.util.function.IntPredicate before, BooleanToDoubleFunction after) {
		return new IntToDoubleFunction() {
			@Override
			public double applyAsDouble(int value) {
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
	static IntToDoubleFunction of(java.util.function.IntToDoubleFunction before, java.util.function.DoubleUnaryOperator after) {
		return new IntToDoubleFunction() {
			@Override
			public double applyAsDouble(int value) {
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
	static IntToDoubleFunction of(java.util.function.IntUnaryOperator before, java.util.function.IntToDoubleFunction after) {
		return new IntToDoubleFunction() {
			@Override
			public double applyAsDouble(int value) {
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
	static IntToDoubleFunction of(java.util.function.IntToLongFunction before, java.util.function.LongToDoubleFunction after) {
		return new IntToDoubleFunction() {
			@Override
			public double applyAsDouble(int value) {
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
	static IntToDoubleFunction when(java.util.function.IntPredicate predicate, java.util.function.IntToDoubleFunction accepted, java.util.function.IntToDoubleFunction rejected) {
		return new IntToDoubleFunction() {
			@Override
			public double applyAsDouble(int value) {
				java.util.function.IntToDoubleFunction function = predicate.test(value) ? accepted : rejected;
				return function.applyAsDouble(value);
			}
		};
	}

	/**
	 * A function that returns its widened parameter.
	 */
	class IdentityIntToDoubleFunction implements IntToDoubleFunction {
		static final IntToDoubleFunction INSTANCE = new IdentityIntToDoubleFunction();

		private IdentityIntToDoubleFunction() {
			// Hide
		}

		@Override
		public double applyAsDouble(int value) {
			return value;
		}
	}
}
