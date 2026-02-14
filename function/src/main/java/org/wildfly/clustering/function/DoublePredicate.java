/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A predicate for a double value.
 * @author Paul Ferraro
 */
public interface DoublePredicate extends java.util.function.DoublePredicate, DoubleOperation, PrimitivePredicate<Double, java.util.function.DoublePredicate> {
	/** A predicate that returns true if its parameter is greater than zero. */
	DoublePredicate POSITIVE = greaterThan(0d);
	/** A predicate that returns true if its parameter is zero. */
	DoublePredicate ZERO = equalTo(0d);
	/** A predicate that returns true if its parameter is less than zero. */
	DoublePredicate NEGATIVE = lessThan(0d);

	@Override
	default Predicate<Double> box() {
		return this.compose(DoubleUnaryOperator.identity().box());
	}

	@Override
	default DoublePredicate and(java.util.function.DoublePredicate other) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return DoublePredicate.this.test(value) && other.test(value);
			}
		};
	}

	@Override
	default DoublePredicate negate() {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return !DoublePredicate.this.test(value);
			}
		};
	}

	@Override
	default DoublePredicate or(java.util.function.DoublePredicate other) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return DoublePredicate.this.test(value) || other.test(value);
			}
		};
	}

	@Override
	default <V> Predicate<V> compose(java.util.function.ToDoubleFunction<? super V> before) {
		return Predicate.of(before, this);
	}

	@Override
	default <V1, V2> BiPredicate<V1, V2> composeBinary(java.util.function.ToDoubleBiFunction<? super V1, ? super V2> before) {
		return BiPredicate.of(before, this);
	}

	@Override
	default BooleanPredicate composeBoolean(BooleanToDoubleFunction before) {
		return BooleanPredicate.of(before, this);
	}

	@Override
	default DoublePredicate composeDouble(java.util.function.DoubleUnaryOperator before) {
		return DoublePredicate.of(before, this);
	}

	@Override
	default IntPredicate composeInt(java.util.function.IntToDoubleFunction before) {
		return IntPredicate.of(before, this);
	}

	@Override
	default LongPredicate composeLong(java.util.function.LongToDoubleFunction before) {
		return LongPredicate.of(before, this);
	}

	@Override
	default DoubleConsumer thenAccept(BooleanConsumer after) {
		return DoubleConsumer.of(this, after);
	}

	@Override
	default <R> DoubleFunction<R> thenApply(BooleanFunction<? extends R> after) {
		return DoubleFunction.of(this, after);
	}

	@Override
	default DoubleUnaryOperator thenApplyAsDouble(BooleanToDoubleFunction after) {
		return DoubleUnaryOperator.of(this, after);
	}

	@Override
	default DoubleToIntFunction thenApplyAsInt(BooleanToIntFunction after) {
		return DoubleToIntFunction.of(this, after);
	}

	@Override
	default DoubleToLongFunction thenApplyAsLong(BooleanToLongFunction after) {
		return DoubleToLongFunction.of(this, after);
	}

	@Override
	default DoubleFunction<Boolean> thenBox() {
		return this.thenApply(BooleanPredicate.identity().thenBox());
	}

	@Override
	default DoublePredicate thenTest(BooleanPredicate after) {
		return DoublePredicate.of(this, after);
	}

	/**
	 * Returns a predicate that always evaluates to the specified result.
	 * @param result the fixed result
	 * @return a predicate that always evaluates to the specified value.
	 */
	static DoublePredicate of(boolean result) {
		return result ? SimpleDoublePredicate.ALWAYS : SimpleDoublePredicate.NEVER;
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static DoublePredicate of(java.util.function.DoubleConsumer before, java.util.function.BooleanSupplier after) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
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
	static DoublePredicate of(java.util.function.DoublePredicate before, BooleanPredicate after) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
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
	static <T> DoublePredicate of(java.util.function.DoubleFunction<? extends T> before, java.util.function.Predicate<? super T> after) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
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
	static DoublePredicate of(java.util.function.DoubleUnaryOperator before, java.util.function.DoublePredicate after) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
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
	static DoublePredicate of(java.util.function.DoubleToIntFunction before, java.util.function.IntPredicate after) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
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
	static DoublePredicate of(java.util.function.DoubleToLongFunction before, java.util.function.LongPredicate after) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return after.test(before.applyAsLong(value));
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param base the comparison value
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static DoublePredicate lessThan(double base) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return Double.compare(value, base) < 0;
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param base the comparison value
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static DoublePredicate equalTo(double base) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return Double.compare(base, value) == 0;
			}
		};
	}

	/**
	 * Returns a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 * @param base the comparison value
	 * @return a predicate that evaluates to true if and only if the argument is equals to the specified object.
	 */
	static DoublePredicate greaterThan(double base) {
		return new DoublePredicate() {
			@Override
			public boolean test(double value) {
				return Double.compare(value, base) > 0;
			}
		};
	}

	/**
	 * A predicate that evaluates to a fixed value.
	 */
	class SimpleDoublePredicate implements DoublePredicate {
		static final DoublePredicate ALWAYS = new SimpleDoublePredicate(true);
		static final DoublePredicate NEVER = new SimpleDoublePredicate(false);

		private final boolean result;

		private SimpleDoublePredicate(boolean result) {
			this.result = result;
		}

		@Override
		public boolean test(double value) {
			return this.result;
		}

		@Override
		public DoublePredicate and(java.util.function.DoublePredicate other) {
			return this.result ? ((other instanceof DoublePredicate predicate) ? predicate : other::test) : NEVER;
		}

		@Override
		public DoublePredicate negate() {
			return this.result ? NEVER : ALWAYS;
		}

		@Override
		public DoublePredicate or(java.util.function.DoublePredicate other) {
			return this.result ? ALWAYS : ((other instanceof DoublePredicate predicate) ? predicate : other::test);
		}

		@Override
		public Predicate<Double> box() {
			return Predicate.of(this.result);
		}

		@Override
		public DoubleFunction<Boolean> thenBox() {
			return DoubleFunction.of(Boolean.valueOf(this.result));
		}
	}
}
