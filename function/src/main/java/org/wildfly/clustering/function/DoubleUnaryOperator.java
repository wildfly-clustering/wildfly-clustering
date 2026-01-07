/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function operating on a double value, returning a double value.
 * @author Paul Ferraro
 */
public interface DoubleUnaryOperator extends java.util.function.DoubleUnaryOperator, DoubleOperation, ToDoubleOperation {

	@Override
	default ToDoubleFunction<Double> box() {
		return this.compose(IdentityDoubleUnaryOperator.INSTANCE.box());
	}

	@Override
	default <T> ToDoubleFunction<T> compose(java.util.function.ToDoubleFunction<? super T> before) {
		return ToDoubleFunction.of(before, this);
	}

	@Override
	default <T1, T2> ToDoubleBiFunction<T1, T2> composeBinary(java.util.function.ToDoubleBiFunction<? super T1, ? super T2> before) {
		return ToDoubleBiFunction.of(before, this);
	}

	@Override
	default BooleanToDoubleFunction composeBoolean(BooleanToDoubleFunction before) {
		return BooleanToDoubleFunction.of(before, this);
	}

	@Override
	default DoubleUnaryOperator composeDouble(java.util.function.DoubleUnaryOperator before) {
		return DoubleUnaryOperator.of(before, this);
	}

	@Override
	default IntToDoubleFunction composeInt(java.util.function.IntToDoubleFunction before) {
		return IntToDoubleFunction.of(before, this);
	}

	@Override
	default LongToDoubleFunction composeLong(java.util.function.LongToDoubleFunction before) {
		return LongToDoubleFunction.of(before, this);
	}

	@Override
	default DoubleConsumer thenAccept(java.util.function.DoubleConsumer after) {
		return DoubleConsumer.of(this, after);
	}

	@Override
	default <R> DoubleFunction<R> thenApply(java.util.function.DoubleFunction<? extends R> after) {
		return DoubleFunction.of(this, after);
	}

	@Override
	default DoubleUnaryOperator thenApplyAsDouble(java.util.function.DoubleUnaryOperator after) {
		return DoubleUnaryOperator.of(this, after);
	}

	@Override
	default DoubleToIntFunction thenApplyAsInt(java.util.function.DoubleToIntFunction after) {
		return DoubleToIntFunction.of(this, after);
	}

	@Override
	default DoubleToLongFunction thenApplyAsLong(java.util.function.DoubleToLongFunction after) {
		return DoubleToLongFunction.of(this, after);
	}

	@Override
	default DoubleFunction<Double> thenBox() {
		return this.thenApply(IdentityDoubleUnaryOperator.INSTANCE.thenBox());
	}

	@Override
	default DoublePredicate thenTest(java.util.function.DoublePredicate after) {
		return DoublePredicate.of(this, after);
	}

	/**
	 * Returns a function that returns its parameter.
	 * @return a function that returns its parameter.
	 */
	static DoubleUnaryOperator identity() {
		return IdentityDoubleUnaryOperator.INSTANCE;
	}

	/**
	 * Returns a function returning the specified value, ignoring its parameter.
	 * @param value the return value
	 * @return a function returning the specified value, ignoring its parameter.
	 */
	static DoubleUnaryOperator of(double value) {
		return new DoubleUnaryOperator() {
			@Override
			public double applyAsDouble(double ignore) {
				return value;
			}

			@Override
			public ToDoubleFunction<Double> box() {
				return ToDoubleFunction.of(value);
			}

			@Override
			public DoubleFunction<Double> thenBox() {
				return DoubleFunction.of(Double.valueOf(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static DoubleUnaryOperator of(java.util.function.DoubleConsumer before, java.util.function.DoubleSupplier after) {
		return new DoubleUnaryOperator() {
			@Override
			public double applyAsDouble(double value) {
				before.accept(value);
				return after.getAsDouble();
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
	static <T> DoubleUnaryOperator of(java.util.function.DoubleFunction<? extends T> before, java.util.function.ToDoubleFunction<? super T> after) {
		return new DoubleUnaryOperator() {
			@Override
			public double applyAsDouble(double value) {
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
	static DoubleUnaryOperator of(java.util.function.DoublePredicate before, BooleanToDoubleFunction after) {
		return new DoubleUnaryOperator() {
			@Override
			public double applyAsDouble(double value) {
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
	static DoubleUnaryOperator of(java.util.function.DoubleUnaryOperator before, java.util.function.DoubleUnaryOperator after) {
		return new DoubleUnaryOperator() {
			@Override
			public double applyAsDouble(double value) {
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
	static DoubleUnaryOperator of(java.util.function.DoubleToIntFunction before, java.util.function.IntToDoubleFunction after) {
		return new DoubleUnaryOperator() {
			@Override
			public double applyAsDouble(double value) {
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
	static DoubleUnaryOperator of(java.util.function.DoubleToLongFunction before, java.util.function.LongToDoubleFunction after) {
		return new DoubleUnaryOperator() {
			@Override
			public double applyAsDouble(double value) {
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
	static DoubleUnaryOperator when(java.util.function.DoublePredicate predicate, java.util.function.DoubleUnaryOperator accepted, java.util.function.DoubleUnaryOperator rejected) {
		return new DoubleUnaryOperator() {
			@Override
			public double applyAsDouble(double value) {
				java.util.function.DoubleUnaryOperator function = predicate.test(value) ? accepted : rejected;
				return function.applyAsDouble(value);
			}
		};
	}

	/**
	 * A function that returns its parameter.
	 */
	class IdentityDoubleUnaryOperator implements DoubleUnaryOperator {
		static final DoubleUnaryOperator INSTANCE = new IdentityDoubleUnaryOperator();
		private static final DoubleFunction<Double> BOX = Double::valueOf;
		private static final ToDoubleFunction<Double> UNBOX = Double::doubleValue;

		private IdentityDoubleUnaryOperator() {
			// Hide
		}

		@Override
		public double applyAsDouble(double value) {
			return value;
		}

		@Override
		public ToDoubleFunction<Double> box() {
			return UNBOX;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> ToDoubleFunction<T> compose(java.util.function.ToDoubleFunction<? super T> before) {
			return (before instanceof ToDoubleFunction<?> function) ? (ToDoubleFunction<T>) function : ToDoubleFunction.of(before, DoubleUnaryOperator.identity());
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T1, T2> ToDoubleBiFunction<T1, T2> composeBinary(java.util.function.ToDoubleBiFunction<? super T1, ? super T2> before) {
			return (before instanceof ToDoubleBiFunction<?, ?> function) ? (ToDoubleBiFunction<T1, T2>) function : ToDoubleBiFunction.of(before, DoubleUnaryOperator.identity());
		}

		@Override
		public BooleanToDoubleFunction composeBoolean(BooleanToDoubleFunction before) {
			return before;
		}

		@Override
		public DoubleUnaryOperator composeDouble(java.util.function.DoubleUnaryOperator before) {
			return (before instanceof DoubleUnaryOperator function) ? function : DoubleUnaryOperator.of(before, DoubleUnaryOperator.identity());
		}

		@Override
		public IntToDoubleFunction composeInt(java.util.function.IntToDoubleFunction before) {
			return (before instanceof IntToDoubleFunction function) ? function : IntToDoubleFunction.of(before, DoubleUnaryOperator.identity());
		}

		@Override
		public LongToDoubleFunction composeLong(java.util.function.LongToDoubleFunction before) {
			return (before instanceof LongToDoubleFunction function) ? function : LongToDoubleFunction.of(before, DoubleUnaryOperator.identity());
		}

		@Override
		public DoubleConsumer thenAccept(java.util.function.DoubleConsumer after) {
			return (after instanceof DoubleConsumer function) ? function : DoubleConsumer.of(DoubleUnaryOperator.identity(), after);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <R> DoubleFunction<R> thenApply(java.util.function.DoubleFunction<? extends R> after) {
			return (after instanceof DoubleFunction<?> function) ? (DoubleFunction<R>) function : DoubleFunction.of(DoubleUnaryOperator.identity(), after);
		}

		@Override
		public DoubleUnaryOperator thenApplyAsDouble(java.util.function.DoubleUnaryOperator after) {
			return (after instanceof DoubleUnaryOperator function) ? function : DoubleUnaryOperator.of(DoubleUnaryOperator.identity(), after);
		}

		@Override
		public DoubleToIntFunction thenApplyAsInt(java.util.function.DoubleToIntFunction after) {
			return (after instanceof DoubleToIntFunction function) ? function : DoubleToIntFunction.of(DoubleUnaryOperator.identity(), after);
		}

		@Override
		public DoubleToLongFunction thenApplyAsLong(java.util.function.DoubleToLongFunction after) {
			return (after instanceof DoubleToLongFunction function) ? function : DoubleToLongFunction.of(DoubleUnaryOperator.identity(), after);
		}

		@Override
		public DoubleFunction<Double> thenBox() {
			return BOX;
		}

		@Override
		public DoublePredicate thenTest(java.util.function.DoublePredicate after) {
			return (after instanceof DoublePredicate function) ? function : DoublePredicate.of(DoubleUnaryOperator.identity(), after);
		}
	}
}
