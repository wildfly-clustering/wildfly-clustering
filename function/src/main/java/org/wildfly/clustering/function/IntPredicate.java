/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An enhanced integer predicate.
 * @author Paul Ferraro
 */
public interface IntPredicate extends java.util.function.IntPredicate, IntOperation, PrimitivePredicate<Integer, java.util.function.IntPredicate> {
	/** A predicate that returns true if the parameter is greater than zero. */
	IntPredicate POSITIVE = greaterThan(0);
	/** A predicate that returns true if the parameter is zero. */
	IntPredicate ZERO = equalTo(0);
	/** A predicate that returns true if the parameter is less than zero. */
	IntPredicate NEGATIVE = lessThan(0);

	@Override
	default Predicate<Integer> box() {
		return this.compose(IntUnaryOperator.identity().box());
	}

	@Override
	default IntPredicate negate() {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return !IntPredicate.this.test(value);
			}
		};
	}

	@Override
	default IntPredicate and(java.util.function.IntPredicate other) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return IntPredicate.this.test(value) && other.test(value);
			}
		};
	}

	@Override
	default IntPredicate or(java.util.function.IntPredicate other) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return IntPredicate.this.test(value) || other.test(value);
			}
		};
	}

	@Override
	default <V> Predicate<V> compose(java.util.function.ToIntFunction<? super V> before) {
		return Predicate.of(before, this);
	}

	@Override
	default <V1, V2> BiPredicate<V1, V2> composeBinary(java.util.function.ToIntBiFunction<? super V1, ? super V2> before) {
		return BiPredicate.of(before, this);
	}

	@Override
	default BooleanPredicate composeBoolean(BooleanToIntFunction before) {
		return BooleanPredicate.of(before, this);
	}

	@Override
	default DoublePredicate composeDouble(java.util.function.DoubleToIntFunction before) {
		return DoublePredicate.of(before, this);
	}

	@Override
	default IntPredicate composeInt(java.util.function.IntUnaryOperator before) {
		return IntPredicate.of(before, this);
	}

	@Override
	default LongPredicate composeLong(java.util.function.LongToIntFunction before) {
		return LongPredicate.of(before, this);
	}

	@Override
	default IntConsumer thenAccept(BooleanConsumer after) {
		return IntConsumer.of(this, after);
	}

	@Override
	default <R> IntFunction<R> thenApply(BooleanFunction<? extends R> after) {
		return IntFunction.of(this, after);
	}

	@Override
	default IntToDoubleFunction thenApplyAsDouble(BooleanToDoubleFunction after) {
		return IntToDoubleFunction.of(this, after);
	}

	@Override
	default IntUnaryOperator thenApplyAsInt(BooleanToIntFunction after) {
		return IntUnaryOperator.of(this, after);
	}

	@Override
	default IntToLongFunction thenApplyAsLong(BooleanToLongFunction after) {
		return IntToLongFunction.of(this, after);
	}

	@Override
	default IntFunction<Boolean> thenBox() {
		return this.thenApply(BooleanPredicate.identity().thenBox());
	}

	@Override
	default IntPredicate thenTest(BooleanPredicate after) {
		return IntPredicate.of(this, after);
	}

	/**
	 * Returns a predicate that always evaluates to the specified result.
	 * @param result the fixed result
	 * @return a predicate that always evaluates to the specified value.
	 */
	static IntPredicate of(boolean result) {
		return result ? SimpleIntPredicate.ALWAYS : SimpleIntPredicate.NEVER;
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static IntPredicate of(java.util.function.IntConsumer before, java.util.function.BooleanSupplier after) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
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
	static IntPredicate of(java.util.function.IntPredicate before, BooleanPredicate after) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
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
	static <T> IntPredicate of(java.util.function.IntFunction<? extends T> before, java.util.function.Predicate<? super T> after) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
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
	static IntPredicate of(java.util.function.IntToDoubleFunction before, java.util.function.DoublePredicate after) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
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
	static IntPredicate of(java.util.function.IntUnaryOperator before, java.util.function.IntPredicate after) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
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
	static IntPredicate of(java.util.function.IntToLongFunction before, java.util.function.LongPredicate after) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return after.test(before.applyAsLong(value));
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param base the comparison value
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static IntPredicate lessThan(int base) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return Integer.compare(value, base) < 0;
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param base the comparison value
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static IntPredicate equalTo(int base) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return Integer.compare(base, value) == 0;
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param base the comparison value
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static IntPredicate greaterThan(int base) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return Integer.compare(value, base) > 0;
			}
		};
	}

	/**
	 * A predicate that evaluates to a fixed value.
	 */
	class SimpleIntPredicate implements IntPredicate {
		static final IntPredicate ALWAYS = new SimpleIntPredicate(true);
		static final IntPredicate NEVER = new SimpleIntPredicate(false);

		private final boolean result;

		private SimpleIntPredicate(boolean result) {
			this.result = result;
		}

		@Override
		public boolean test(int value) {
			return this.result;
		}

		@Override
		public IntPredicate and(java.util.function.IntPredicate other) {
			return this.result ? ((other instanceof IntPredicate predicate) ? predicate : other::test) : NEVER;
		}

		@Override
		public IntPredicate negate() {
			return this.result ? NEVER : ALWAYS;
		}

		@Override
		public IntPredicate or(java.util.function.IntPredicate other) {
			return this.result ? ALWAYS : ((other instanceof IntPredicate predicate) ? predicate : other::test);
		}

		@Override
		public Predicate<Integer> box() {
			return Predicate.of(this.result);
		}

		@Override
		public IntFunction<Boolean> thenBox() {
			return IntFunction.of(Boolean.valueOf(this.result));
		}
	}
}
