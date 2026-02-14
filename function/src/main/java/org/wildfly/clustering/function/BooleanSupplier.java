/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A supplier of a boolean value.
 * @author Paul Ferraro
 */
public interface BooleanSupplier extends java.util.function.BooleanSupplier, PrimitiveSupplier<Boolean>, ToBooleanOperation {

	@Override
	default Supplier<Boolean> thenBox() {
		return this.thenApply(BooleanPredicate.identity().thenBox());
	}

	@Override
	default BooleanSupplier compose(Runnable before) {
		return BooleanSupplier.of(before, this);
	}

	@Override
	default <T> Predicate<T> compose(java.util.function.Consumer<? super T> before) {
		return Predicate.of(before, this);
	}

	@Override
	default <T1, T2> BiPredicate<T1, T2> composeBinary(java.util.function.BiConsumer<? super T1, ? super T2> before) {
		return BiPredicate.of(before, this);
	}

	@Override
	default BooleanPredicate composeBoolean(BooleanConsumer before) {
		return BooleanPredicate.of(before, this);
	}

	@Override
	default DoublePredicate composeDouble(java.util.function.DoubleConsumer before) {
		return DoublePredicate.of(before, this);
	}

	@Override
	default IntPredicate composeInt(java.util.function.IntConsumer before) {
		return IntPredicate.of(before, this);
	}

	@Override
	default LongPredicate composeLong(java.util.function.LongConsumer before) {
		return LongPredicate.of(before, this);
	}

	@Override
	default Runner thenAccept(BooleanConsumer consumer) {
		return Runner.of(this, consumer);
	}

	@Override
	default <R> Supplier<R> thenApply(BooleanFunction<? extends R> after) {
		return Supplier.of(this, after);
	}

	@Override
	default DoubleSupplier thenApplyAsDouble(BooleanToDoubleFunction after) {
		return DoubleSupplier.of(this, after);
	}

	@Override
	default IntSupplier thenApplyAsInt(BooleanToIntFunction after) {
		return IntSupplier.of(this, after);
	}

	@Override
	default LongSupplier thenApplyAsLong(BooleanToLongFunction after) {
		return LongSupplier.of(this, after);
	}

	@Override
	default BooleanSupplier thenTest(BooleanPredicate after) {
		return BooleanSupplier.of(this, after);
	}

	/**
	 * Returns a supplier that always returns the specified value.
	 * @param value the supplied value
	 * @return a supplier that always returns the specified value.
	 */
	static BooleanSupplier of(boolean value) {
		return value ? SimpleBooleanSupplier.TRUE : SimpleBooleanSupplier.FALSE;
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static BooleanSupplier of(java.lang.Runnable before, java.util.function.BooleanSupplier after) {
		return new BooleanSupplier() {
			@Override
			public boolean getAsBoolean() {
				before.run();
				return after.getAsBoolean();
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param <T> the intermediate type
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static <T> BooleanSupplier of(java.util.function.Supplier<? extends T> before, java.util.function.Predicate<? super T> after) {
		return new BooleanSupplier() {
			@Override
			public boolean getAsBoolean() {
				return after.test(before.get());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static BooleanSupplier of(java.util.function.BooleanSupplier before, BooleanPredicate after) {
		return new BooleanSupplier() {
			@Override
			public boolean getAsBoolean() {
				return after.test(before.getAsBoolean());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static BooleanSupplier of(java.util.function.DoubleSupplier before, java.util.function.DoublePredicate after) {
		return new BooleanSupplier() {
			@Override
			public boolean getAsBoolean() {
				return after.test(before.getAsDouble());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static BooleanSupplier of(java.util.function.IntSupplier before, java.util.function.IntPredicate after) {
		return new BooleanSupplier() {
			@Override
			public boolean getAsBoolean() {
				return after.test(before.getAsInt());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static BooleanSupplier of(java.util.function.LongSupplier before, java.util.function.LongPredicate after) {
		return new BooleanSupplier() {
			@Override
			public boolean getAsBoolean() {
				return after.test(before.getAsLong());
			}
		};
	}

	/**
	 * A supplier that returns a fixed value.
	 */
	class SimpleBooleanSupplier implements BooleanSupplier {
		private static final BooleanSupplier FALSE = new SimpleBooleanSupplier(false);
		private static final BooleanSupplier TRUE = new SimpleBooleanSupplier(true);

		private final boolean value;

		private SimpleBooleanSupplier(boolean value) {
			this.value = value;
		}

		@Override
		public boolean getAsBoolean() {
			return this.value;
		}

		@Override
		public Supplier<Boolean> thenBox() {
			return Supplier.of(Boolean.valueOf(this.value));
		}
	}
}
