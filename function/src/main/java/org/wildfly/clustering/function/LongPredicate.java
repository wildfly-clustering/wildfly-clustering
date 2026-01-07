/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;


/**
 * An enhanced long predicate.
 * @author Paul Ferraro
 */
public interface LongPredicate extends java.util.function.LongPredicate, LongOperation, PrimitivePredicate<Long, java.util.function.LongPredicate> {
	/** A predicate that returns true if its parameter is greater than zero. */
	LongPredicate POSITIVE = greaterThan(0L);
	/** A predicate that returns true if its parameter is zero. */
	LongPredicate ZERO = equalTo(0L);
	/** A predicate that returns true if its parameter is less than zero. */
	LongPredicate NEGATIVE = lessThan(0L);

	@Override
	default Predicate<Long> box() {
		return null;
	}

	@Override
	default LongPredicate and(java.util.function.LongPredicate other) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return LongPredicate.this.test(value) && other.test(value);
			}
		};
	}

	@Override
	default LongPredicate negate() {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return !LongPredicate.this.test(value);
			}
		};
	}

	@Override
	default LongPredicate or(java.util.function.LongPredicate other) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return LongPredicate.this.test(value) || other.test(value);
			}
		};
	}

	@Override
	default <V> Predicate<V> compose(java.util.function.ToLongFunction<? super V> before) {
		return Predicate.of(before, this);
	}

	@Override
	default <V1, V2> BiPredicate<V1, V2> composeBinary(java.util.function.ToLongBiFunction<? super V1, ? super V2> before) {
		return BiPredicate.of(before, this);
	}

	@Override
	default BooleanPredicate composeBoolean(BooleanToLongFunction before) {
		return BooleanPredicate.of(before, this);
	}

	@Override
	default DoublePredicate composeDouble(java.util.function.DoubleToLongFunction before) {
		return DoublePredicate.of(before, this);
	}

	@Override
	default IntPredicate composeInt(java.util.function.IntToLongFunction before) {
		return IntPredicate.of(before, this);
	}

	@Override
	default LongPredicate composeLong(java.util.function.LongUnaryOperator before) {
		return LongPredicate.of(before, this);
	}

	@Override
	default LongConsumer thenAccept(BooleanConsumer after) {
		return LongConsumer.of(this, after);
	}

	@Override
	default <R> LongFunction<R> thenApply(BooleanFunction<? extends R> after) {
		return LongFunction.of(this, after);
	}

	@Override
	default LongToDoubleFunction thenApplyAsDouble(BooleanToDoubleFunction after) {
		return LongToDoubleFunction.of(this, after);
	}

	@Override
	default LongToIntFunction thenApplyAsInt(BooleanToIntFunction after) {
		return LongToIntFunction.of(this, after);
	}

	@Override
	default LongUnaryOperator thenApplyAsLong(BooleanToLongFunction after) {
		return LongUnaryOperator.of(this, after);
	}

	@Override
	default LongPredicate thenTest(BooleanPredicate after) {
		return LongPredicate.of(this, after);
	}

	@Override
	default LongFunction<Boolean> thenBox() {
		return this.thenApply(BooleanPredicate.identity().thenBox());
	}

	/**
	 * Returns a predicate that always evaluates to the specified result.
	 * @param result the fixed result
	 * @return a predicate that always evaluates to the specified value.
	 */
	static LongPredicate of(boolean result) {
		return result ? SimpleLongPredicate.ALWAYS : SimpleLongPredicate.NEVER;
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static LongPredicate of(java.util.function.LongConsumer before, java.util.function.BooleanSupplier after) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				before.accept(value);
				return after.getAsBoolean();
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static LongPredicate of(java.util.function.LongPredicate before, BooleanPredicate after) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return after.test(before.test(value));
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param <T> the intermediate type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static <T> LongPredicate of(java.util.function.LongFunction<? extends T> before, java.util.function.Predicate<? super T> after) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return after.test(before.apply(value));
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static LongPredicate of(java.util.function.LongToDoubleFunction before, java.util.function.DoublePredicate after) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return after.test(before.applyAsDouble(value));
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static LongPredicate of(java.util.function.LongToIntFunction before, java.util.function.IntPredicate after) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return after.test(before.applyAsInt(value));
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static LongPredicate of(java.util.function.LongUnaryOperator before, java.util.function.LongPredicate after) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return after.test(before.applyAsLong(value));
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param base the comparison value
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static LongPredicate lessThan(long base) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return Long.compare(value, base) < 0L;
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param base the comparison value
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static LongPredicate equalTo(long base) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return Long.compare(base, value) == 0L;
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param base the comparison value
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static LongPredicate greaterThan(long base) {
		return new LongPredicate() {
			@Override
			public boolean test(long value) {
				return Long.compare(value, base) > 0L;
			}
		};
	}

	/**
	 * A predicate that evaluates to a fixed value.
	 */
	class SimpleLongPredicate implements LongPredicate {
		static final LongPredicate ALWAYS = new SimpleLongPredicate(true);
		static final LongPredicate NEVER = new SimpleLongPredicate(false);

		private final boolean result;

		private SimpleLongPredicate(boolean result) {
			this.result = result;
		}

		@Override
		public boolean test(long value) {
			return this.result;
		}

		@Override
		public LongPredicate and(java.util.function.LongPredicate other) {
			return this.result ? ((other instanceof LongPredicate predicate) ? predicate : other::test) : NEVER;
		}

		@Override
		public LongPredicate negate() {
			return this.result ? NEVER : ALWAYS;
		}

		@Override
		public LongPredicate or(java.util.function.LongPredicate other) {
			return this.result ? ALWAYS : ((other instanceof LongPredicate predicate) ? predicate : other::test);
		}

		@Override
		public Predicate<Long> box() {
			return Predicate.of(this.result);
		}

		@Override
		public LongFunction<Boolean> thenBox() {
			return LongFunction.of(Boolean.valueOf(this.result));
		}
	}
}
