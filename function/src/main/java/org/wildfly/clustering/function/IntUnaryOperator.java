/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function operating on an int value, returning an int value.
 * @author Paul Ferraro
 */
public interface IntUnaryOperator extends java.util.function.IntUnaryOperator, IntOperation, ToIntOperation {

	@Override
	default ToIntFunction<Integer> box() {
		return this.compose(IdentityIntUnaryOperator.INSTANCE.box());
	}

	@Override
	default <T> ToIntFunction<T> compose(java.util.function.ToIntFunction<? super T> before) {
		return ToIntFunction.of(before, this);
	}

	@Override
	default <T1, T2> ToIntBiFunction<T1, T2> composeBinary(java.util.function.ToIntBiFunction<? super T1, ? super T2> before) {
		return ToIntBiFunction.of(before, this);
	}

	@Override
	default BooleanToIntFunction composeBoolean(BooleanToIntFunction before) {
		return BooleanToIntFunction.of(before, this);
	}

	@Override
	default DoubleToIntFunction composeDouble(java.util.function.DoubleToIntFunction before) {
		return DoubleToIntFunction.of(before, this);
	}

	@Override
	default IntUnaryOperator composeInt(java.util.function.IntUnaryOperator before) {
		return IntUnaryOperator.of(before, this);
	}

	@Override
	default LongToIntFunction composeLong(java.util.function.LongToIntFunction before) {
		return LongToIntFunction.of(before, this);
	}

	@Override
	default IntConsumer thenAccept(java.util.function.IntConsumer after) {
		return IntConsumer.of(this, after);
	}

	@Override
	default <R> IntFunction<R> thenApply(java.util.function.IntFunction<? extends R> after) {
		return IntFunction.of(this, after);
	}

	@Override
	default IntToDoubleFunction thenApplyAsDouble(java.util.function.IntToDoubleFunction after) {
		return IntToDoubleFunction.of(this, after);
	}

	@Override
	default IntUnaryOperator thenApplyAsInt(java.util.function.IntUnaryOperator after) {
		return IntUnaryOperator.of(this, after);
	}

	@Override
	default IntToLongFunction thenApplyAsLong(java.util.function.IntToLongFunction after) {
		return IntToLongFunction.of(this, after);
	}

	@Override
	default IntFunction<Integer> thenBox() {
		return this.thenApply(IdentityIntUnaryOperator.INSTANCE.thenBox());
	}

	@Override
	default IntPredicate thenTest(java.util.function.IntPredicate after) {
		return IntPredicate.of(this, after);
	}

	/**
	 * Returns a function that returns its parameter.
	 * @return a function that returns its parameter.
	 */
	static IntUnaryOperator identity() {
		return IdentityIntUnaryOperator.INSTANCE;
	}

	/**
	 * Returns a function returning the specified value, ignoring its parameter.
	 * @param value the return value
	 * @return a function returning the specified value, ignoring its parameter.
	 */
	static IntUnaryOperator of(int value) {
		return new IntUnaryOperator() {
			@Override
			public int applyAsInt(int ignore) {
				return value;
			}

			@Override
			public ToIntFunction<Integer> box() {
				return ToIntFunction.of(value);
			}

			@Override
			public IntFunction<Integer> thenBox() {
				return IntFunction.of(Integer.valueOf(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static IntUnaryOperator of(java.util.function.IntConsumer before, java.util.function.IntSupplier after) {
		return new IntUnaryOperator() {
			@Override
			public int applyAsInt(int value) {
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
	static <T> IntUnaryOperator of(java.util.function.IntFunction<? extends T> before, java.util.function.ToIntFunction<? super T> after) {
		return new IntUnaryOperator() {
			@Override
			public int applyAsInt(int value) {
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
	static IntUnaryOperator of(java.util.function.IntPredicate before, BooleanToIntFunction after) {
		return new IntUnaryOperator() {
			@Override
			public int applyAsInt(int value) {
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
	static IntUnaryOperator of(java.util.function.IntToDoubleFunction before, java.util.function.DoubleToIntFunction after) {
		return new IntUnaryOperator() {
			@Override
			public int applyAsInt(int value) {
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
	static IntUnaryOperator of(java.util.function.IntUnaryOperator before, java.util.function.IntUnaryOperator after) {
		return new IntUnaryOperator() {
			@Override
			public int applyAsInt(int value) {
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
	static IntUnaryOperator of(java.util.function.IntToLongFunction before, java.util.function.LongToIntFunction after) {
		return new IntUnaryOperator() {
			@Override
			public int applyAsInt(int value) {
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
	static IntUnaryOperator when(java.util.function.IntPredicate predicate, java.util.function.IntUnaryOperator accepted, java.util.function.IntUnaryOperator rejected) {
		return new IntUnaryOperator() {
			@Override
			public int applyAsInt(int value) {
				java.util.function.IntUnaryOperator function = predicate.test(value) ? accepted : rejected;
				return function.applyAsInt(value);
			}
		};
	}

	/**
	 * A function that returns its parameter.
	 */
	class IdentityIntUnaryOperator implements IntUnaryOperator {
		static final IntUnaryOperator INSTANCE = new IdentityIntUnaryOperator();
		private static final IntFunction<Integer> BOX = Integer::valueOf;
		private static final ToIntFunction<Integer> UNBOX = Integer::intValue;

		private IdentityIntUnaryOperator() {
			// Hide
		}

		@Override
		public int applyAsInt(int value) {
			return value;
		}

		@Override
		public ToIntFunction<Integer> box() {
			return UNBOX;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> ToIntFunction<T> compose(java.util.function.ToIntFunction<? super T> before) {
			return (before instanceof ToIntFunction<?> function) ? (ToIntFunction<T>) function : ToIntFunction.of(before, IntUnaryOperator.identity());
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T1, T2> ToIntBiFunction<T1, T2> composeBinary(java.util.function.ToIntBiFunction<? super T1, ? super T2> before) {
			return (before instanceof ToIntBiFunction<?, ?> function) ? (ToIntBiFunction<T1, T2>) function : ToIntBiFunction.of(before, IntUnaryOperator.identity());
		}

		@Override
		public BooleanToIntFunction composeBoolean(BooleanToIntFunction before) {
			return before;
		}

		@Override
		public DoubleToIntFunction composeDouble(java.util.function.DoubleToIntFunction before) {
			return (before instanceof DoubleToIntFunction function) ? function : DoubleToIntFunction.of(before, IntUnaryOperator.identity());
		}

		@Override
		public IntUnaryOperator composeInt(java.util.function.IntUnaryOperator before) {
			return (before instanceof IntUnaryOperator function) ? function : IntUnaryOperator.of(before, IntUnaryOperator.identity());
		}

		@Override
		public LongToIntFunction composeLong(java.util.function.LongToIntFunction before) {
			return (before instanceof LongToIntFunction function) ? function : LongToIntFunction.of(before, IntUnaryOperator.identity());
		}

		@Override
		public IntConsumer thenAccept(java.util.function.IntConsumer after) {
			return (after instanceof IntConsumer consumer) ? consumer : IntConsumer.of(IntUnaryOperator.identity(), after);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <R> IntFunction<R> thenApply(java.util.function.IntFunction<? extends R> after) {
			return (after instanceof IntFunction<?> function) ? (IntFunction<R>) function : IntFunction.of(IntUnaryOperator.identity(), after);
		}

		@Override
		public IntToDoubleFunction thenApplyAsDouble(java.util.function.IntToDoubleFunction after) {
			return IntUnaryOperator.super.thenApplyAsDouble(after);
		}

		@Override
		public IntUnaryOperator thenApplyAsInt(java.util.function.IntUnaryOperator after) {
			return (after instanceof IntUnaryOperator function) ? function : IntUnaryOperator.of(IntUnaryOperator.identity(), after);
		}

		@Override
		public IntToLongFunction thenApplyAsLong(java.util.function.IntToLongFunction after) {
			return (after instanceof IntToLongFunction function) ? function : IntToLongFunction.of(IntUnaryOperator.identity(), after);
		}

		@Override
		public IntPredicate thenTest(java.util.function.IntPredicate after) {
			return (after instanceof IntPredicate predicate) ? predicate : IntPredicate.of(IntUnaryOperator.identity(), after);
		}

		@Override
		public IntFunction<Integer> thenBox() {
			return BOX;
		}
	}
}
