/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A predicate for a boolean value.
 * @author Paul Ferraro
 */
public interface BooleanPredicate extends BooleanOperation, PrimitivePredicate<Boolean, BooleanPredicate> {
	/**
	 * Tests the specified value.
	 * @param value the predicate parameter
	 * @return the test result
	 */
	boolean test(boolean value);

	@Override
	default Predicate<Boolean> box() {
		return this.compose(IdentityBooleanPredicate.INSTANCE.box());
	}

	@Override
	default BooleanPredicate negate() {
		return new BooleanPredicate() {
			@Override
			public boolean test(boolean value) {
				return !value;
			}
		};
	}

	@Override
	default BooleanPredicate and(BooleanPredicate other) {
		return new BooleanPredicate() {
			@Override
			public boolean test(boolean value) {
				return value && BooleanPredicate.this.test(value);
			}
		};
	}

	@Override
	default BooleanPredicate or(BooleanPredicate other) {
		return new BooleanPredicate() {
			@Override
			public boolean test(boolean value) {
				return value || BooleanPredicate.this.test(value);
			}
		};
	}

	@Override
	default <V> Predicate<V> compose(java.util.function.Predicate<? super V> before) {
		return Predicate.of(before, this);
	}

	@Override
	default <V1, V2> BiPredicate<V1, V2> composeBinary(java.util.function.BiPredicate<? super V1, ? super V2> before) {
		return BiPredicate.of(before, this);
	}

	@Override
	default BooleanPredicate composeBoolean(BooleanPredicate before) {
		return BooleanPredicate.of(before, this);
	}

	@Override
	default DoublePredicate composeDouble(java.util.function.DoublePredicate before) {
		return DoublePredicate.of(before, this);
	}

	@Override
	default IntPredicate composeInt(java.util.function.IntPredicate before) {
		return IntPredicate.of(before, this);
	}

	@Override
	default LongPredicate composeLong(java.util.function.LongPredicate before) {
		return LongPredicate.of(before, this);
	}

	@Override
	default BooleanConsumer thenAccept(BooleanConsumer after) {
		return BooleanConsumer.of(this, after);
	}

	@Override
	default <R> BooleanFunction<R> thenApply(BooleanFunction<? extends R> after) {
		return BooleanFunction.of(this, after);
	}

	@Override
	default BooleanToDoubleFunction thenApplyAsDouble(BooleanToDoubleFunction after) {
		return BooleanToDoubleFunction.of(this, after);
	}

	@Override
	default BooleanToIntFunction thenApplyAsInt(BooleanToIntFunction after) {
		return BooleanToIntFunction.of(this, after);
	}

	@Override
	default BooleanToLongFunction thenApplyAsLong(BooleanToLongFunction after) {
		return BooleanToLongFunction.of(this, after);
	}

	@Override
	default BooleanFunction<Boolean> thenBox() {
		return this.thenApply(IdentityBooleanPredicate.INSTANCE.thenBox());
	}

	@Override
	default BooleanPredicate thenTest(BooleanPredicate after) {
		return BooleanPredicate.of(this, after);
	}

	/**
	 * Returns a predicate that returns its parameter.
	 * @return a predicate that returns its parameter.
	 */
	static BooleanPredicate identity() {
		return IdentityBooleanPredicate.INSTANCE;
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static BooleanPredicate of(BooleanConsumer before, java.util.function.BooleanSupplier after) {
		return new BooleanPredicate() {
			@Override
			public boolean test(boolean value) {
				before.accept(value);
				return after.getAsBoolean();
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
	static <T> BooleanPredicate of(BooleanFunction<? extends T> before, java.util.function.Predicate<? super T> after) {
		return new BooleanPredicate() {
			@Override
			public boolean test(boolean value) {
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
	static BooleanPredicate of(BooleanPredicate before, BooleanPredicate after) {
		return new BooleanPredicate() {
			@Override
			public boolean test(boolean value) {
				return after.test(before.test(value));
			}
		};
	}

	/**
	 * Composes a predicate from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite predicate
	 */
	static BooleanPredicate of(BooleanToDoubleFunction before, java.util.function.DoublePredicate after) {
		return new BooleanPredicate() {
			@Override
			public boolean test(boolean value) {
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
	static BooleanPredicate of(BooleanToIntFunction before, java.util.function.IntPredicate after) {
		return new BooleanPredicate() {
			@Override
			public boolean test(boolean value) {
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
	static BooleanPredicate of(BooleanToLongFunction before, java.util.function.LongPredicate after) {
		return new BooleanPredicate() {
			@Override
			public boolean test(boolean value) {
				return after.test(before.applyAsLong(value));
			}
		};
	}

	/**
	 * A predicate that evaluates to a fixed value.
	 */
	class SimpleBooleanPredicate implements BooleanPredicate {
		static final BooleanPredicate ALWAYS = new SimpleBooleanPredicate(true);
		static final BooleanPredicate NEVER = new SimpleBooleanPredicate(false);

		private final boolean result;

		private SimpleBooleanPredicate(boolean result) {
			this.result = result;
		}

		@Override
		public boolean test(boolean value) {
			return this.result;
		}

		@Override
		public BooleanPredicate and(BooleanPredicate other) {
			return this.result ? other : NEVER;
		}

		@Override
		public BooleanPredicate negate() {
			return this.result ? NEVER : ALWAYS;
		}

		@Override
		public BooleanPredicate or(BooleanPredicate other) {
			return this.result ? ALWAYS : other;
		}
	}

	/**
	 * A predicate that evaluates to its parameter value.
	 */
	class IdentityBooleanPredicate implements BooleanPredicate {
		static final BooleanPredicate INSTANCE = new IdentityBooleanPredicate();
		private static final BooleanFunction<Boolean> BOX = Boolean::valueOf;
		private static final Predicate<Boolean> UNBOX = Boolean::booleanValue;

		private IdentityBooleanPredicate() {
			// Hide
		}

		@Override
		public boolean test(boolean value) {
			return value;
		}

		@Override
		public Predicate<Boolean> box() {
			return UNBOX;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V> Predicate<V> compose(java.util.function.Predicate<? super V> before) {
			return (before instanceof Predicate<?> predicate) ? (Predicate<V>) predicate : before::test;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V1, V2> BiPredicate<V1, V2> composeBinary(java.util.function.BiPredicate<? super V1, ? super V2> before) {
			return (before instanceof BiPredicate<?, ?> predicate) ? (BiPredicate<V1, V2>) predicate : before::test;
		}

		@Override
		public BooleanPredicate composeBoolean(BooleanPredicate before) {
			return before;
		}

		@Override
		public DoublePredicate composeDouble(java.util.function.DoublePredicate before) {
			return (before instanceof DoublePredicate predicate) ? predicate : before::test;
		}

		@Override
		public IntPredicate composeInt(java.util.function.IntPredicate before) {
			return (before instanceof IntPredicate predicate) ? predicate : before::test;
		}

		@Override
		public LongPredicate composeLong(java.util.function.LongPredicate before) {
			return (before instanceof LongPredicate predicate) ? predicate : before::test;
		}

		@Override
		public BooleanConsumer thenAccept(BooleanConsumer after) {
			return after;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <R> BooleanFunction<R> thenApply(BooleanFunction<? extends R> after) {
			return (BooleanFunction<R>) after;
		}

		@Override
		public BooleanToDoubleFunction thenApplyAsDouble(BooleanToDoubleFunction after) {
			return after;
		}

		@Override
		public BooleanToIntFunction thenApplyAsInt(BooleanToIntFunction after) {
			return after;
		}

		@Override
		public BooleanToLongFunction thenApplyAsLong(BooleanToLongFunction after) {
			return after;
		}

		@Override
		public BooleanPredicate thenTest(BooleanPredicate after) {
			return after;
		}

		@Override
		public BooleanFunction<Boolean> thenBox() {
			return BOX;
		}
	}
}
