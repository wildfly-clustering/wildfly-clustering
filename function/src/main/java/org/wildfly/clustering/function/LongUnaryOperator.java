/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function operating on a long value, returning a long value.
 * @author Paul Ferraro
 */
public interface LongUnaryOperator extends java.util.function.LongUnaryOperator, LongOperation, ToLongOperation {

	@Override
	default ToLongFunction<Long> box() {
		return this.compose(IdentityLongUnaryOperator.INSTANCE.box());
	}

	@Override
	default <T> ToLongFunction<T> compose(java.util.function.ToLongFunction<? super T> before) {
		return ToLongFunction.of(before, this);
	}

	@Override
	default <T1, T2> ToLongBiFunction<T1, T2> composeBinary(java.util.function.ToLongBiFunction<? super T1, ? super T2> before) {
		return ToLongBiFunction.of(before, this);
	}

	@Override
	default BooleanToLongFunction composeBoolean(BooleanToLongFunction before) {
		return BooleanToLongFunction.of(before, this);
	}

	@Override
	default DoubleToLongFunction composeDouble(java.util.function.DoubleToLongFunction before) {
		return DoubleToLongFunction.of(before, this);
	}

	@Override
	default IntToLongFunction composeInt(java.util.function.IntToLongFunction before) {
		return IntToLongFunction.of(before, this);
	}

	@Override
	default LongUnaryOperator composeLong(java.util.function.LongUnaryOperator before) {
		return LongUnaryOperator.of(before, this);
	}

	@Override
	default LongConsumer thenAccept(java.util.function.LongConsumer after) {
		return LongConsumer.of(this, after);
	}

	@Override
	default <R> LongFunction<R> thenApply(java.util.function.LongFunction<? extends R> after) {
		return LongFunction.of(this, after);
	}

	@Override
	default LongToDoubleFunction thenApplyAsDouble(java.util.function.LongToDoubleFunction after) {
		return LongToDoubleFunction.of(this, after);
	}

	@Override
	default LongToIntFunction thenApplyAsInt(java.util.function.LongToIntFunction after) {
		return LongToIntFunction.of(this, after);
	}

	@Override
	default LongUnaryOperator thenApplyAsLong(java.util.function.LongUnaryOperator after) {
		return LongUnaryOperator.of(this, after);
	}

	@Override
	default LongFunction<Long> thenBox() {
		return this.thenApply(IdentityLongUnaryOperator.INSTANCE.thenBox());
	}

	@Override
	default LongPredicate thenTest(java.util.function.LongPredicate after) {
		return LongPredicate.of(this, after);
	}

	/**
	 * Returns a function that returns its parameter.
	 * @return a function that returns its parameter.
	 */
	static LongUnaryOperator identity() {
		return IdentityLongUnaryOperator.INSTANCE;
	}

	/**
	 * Returns a function returning the specified value, ignoring its parameter.
	 * @param value the return value
	 * @return a function returning the specified value, ignoring its parameter.
	 */
	static LongUnaryOperator of(long value) {
		return new LongUnaryOperator() {
			@Override
			public long applyAsLong(long ignore) {
				return value;
			}

			@Override
			public ToLongFunction<Long> box() {
				return ToLongFunction.of(value);
			}

			@Override
			public LongFunction<Long> thenBox() {
				return LongFunction.of(Long.valueOf(value));
			}
		};
	}

	/**
	 * Composes a function from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite function
	 */
	static LongUnaryOperator of(java.util.function.LongConsumer before, java.util.function.LongSupplier after) {
		return new LongUnaryOperator() {
			@Override
			public long applyAsLong(long value) {
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
	static <T> LongUnaryOperator of(java.util.function.LongFunction<? extends T> before, java.util.function.ToLongFunction<? super T> after) {
		return new LongUnaryOperator() {
			@Override
			public long applyAsLong(long value) {
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
	static LongUnaryOperator of(java.util.function.LongPredicate before, BooleanToLongFunction after) {
		return new LongUnaryOperator() {
			@Override
			public long applyAsLong(long value) {
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
	static LongUnaryOperator of(java.util.function.LongToDoubleFunction before, java.util.function.DoubleToLongFunction after) {
		return new LongUnaryOperator() {
			@Override
			public long applyAsLong(long value) {
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
	static LongUnaryOperator of(java.util.function.LongToIntFunction before, java.util.function.IntToLongFunction after) {
		return new LongUnaryOperator() {
			@Override
			public long applyAsLong(long value) {
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
	static LongUnaryOperator of(java.util.function.LongUnaryOperator before, java.util.function.LongUnaryOperator after) {
		return new LongUnaryOperator() {
			@Override
			public long applyAsLong(long value) {
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
	static LongUnaryOperator when(java.util.function.LongPredicate predicate, java.util.function.LongUnaryOperator accepted, java.util.function.LongUnaryOperator rejected) {
		return new LongUnaryOperator() {
			@Override
			public long applyAsLong(long value) {
				java.util.function.LongUnaryOperator function = predicate.test(value) ? accepted : rejected;
				return function.applyAsLong(value);
			}
		};
	}

	/**
	 * A function that returns its parameter.
	 */
	class IdentityLongUnaryOperator implements LongUnaryOperator {
		static final LongUnaryOperator INSTANCE = new IdentityLongUnaryOperator();
		private static final LongFunction<Long> BOX = Long::valueOf;
		private static final ToLongFunction<Long> UNBOX = Long::longValue;

		private IdentityLongUnaryOperator() {
			// Hide
		}

		@Override
		public long applyAsLong(long value) {
			return value;
		}

		@Override
		public ToLongFunction<Long> box() {
			return UNBOX;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> ToLongFunction<T> compose(java.util.function.ToLongFunction<? super T> before) {
			return (before instanceof ToLongFunction<?> function) ? (ToLongFunction<T>) function : before::applyAsLong;
		}

		@Override
		public DoubleToLongFunction composeDouble(java.util.function.DoubleToLongFunction before) {
			return (before instanceof DoubleToLongFunction function) ? function : before::applyAsLong;
		}

		@Override
		public IntToLongFunction composeInt(java.util.function.IntToLongFunction before) {
			return (before instanceof IntToLongFunction function) ? function : before::applyAsLong;
		}

		@Override
		public LongUnaryOperator composeLong(java.util.function.LongUnaryOperator before) {
			return (before instanceof LongUnaryOperator function) ? function : before::applyAsLong;
		}

		@Override
		public LongConsumer thenAccept(java.util.function.LongConsumer after) {
			return (after instanceof LongConsumer consumer) ? consumer : after::accept;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <R> LongFunction<R> thenApply(java.util.function.LongFunction<? extends R> after) {
			return (after instanceof LongFunction<?> function) ? (LongFunction<R>) function : after::apply;
		}

		@Override
		public LongToDoubleFunction thenApplyAsDouble(java.util.function.LongToDoubleFunction after) {
			return (after instanceof LongToDoubleFunction function) ? function : after::applyAsDouble;
		}

		@Override
		public LongToIntFunction thenApplyAsInt(java.util.function.LongToIntFunction after) {
			return (after instanceof LongToIntFunction function) ? function : after::applyAsInt;
		}

		@Override
		public LongUnaryOperator thenApplyAsLong(java.util.function.LongUnaryOperator after) {
			return (after instanceof LongUnaryOperator function) ? function : after::applyAsLong;
		}

		@Override
		public LongFunction<Long> thenBox() {
			return BOX;
		}

		@Override
		public LongPredicate thenTest(java.util.function.LongPredicate after) {
			return (after instanceof LongPredicate predicate) ? predicate : after::test;
		}
	}
}
