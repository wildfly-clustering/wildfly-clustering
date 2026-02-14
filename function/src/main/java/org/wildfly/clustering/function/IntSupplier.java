/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * Enhanced {@link java.util.function.IntSupplier}.
 * @author Paul Ferraro
 */
public interface IntSupplier extends java.util.function.IntSupplier, PrimitiveSupplier<Integer>, ToIntOperation {
	/** A supplier that always returns {@value Integer#MIN_VALUE}. */
	IntSupplier MINIMUM = of(Integer.MIN_VALUE);
	/** A supplier that always returns zero. */
	IntSupplier ZERO = of(0);
	/** A supplier that always returns {@value Integer#MAX_VALUE}. */
	IntSupplier MAXIMUM = of(Integer.MAX_VALUE);

	@Override
	default IntSupplier compose(Runnable before) {
		return IntSupplier.of(before, this);
	}

	@Override
	default <T> ToIntFunction<T> compose(java.util.function.Consumer<? super T> before) {
		return ToIntFunction.of(before, this);
	}

	@Override
	default <T1, T2> ToIntBiFunction<T1, T2> composeBinary(java.util.function.BiConsumer<? super T1, ? super T2> before) {
		return ToIntBiFunction.of(before, this);
	}

	@Override
	default BooleanToIntFunction composeBoolean(BooleanConsumer before) {
		return BooleanToIntFunction.of(before, this);
	}

	@Override
	default DoubleToIntFunction composeDouble(java.util.function.DoubleConsumer before) {
		return DoubleToIntFunction.of(before, this);
	}

	@Override
	default IntUnaryOperator composeInt(java.util.function.IntConsumer before) {
		return IntUnaryOperator.of(before, this);
	}

	@Override
	default LongToIntFunction composeLong(java.util.function.LongConsumer before) {
		return LongToIntFunction.of(before, this);
	}

	@Override
	default Runner thenAccept(java.util.function.IntConsumer consumer) {
		return Runner.of(this, consumer);
	}

	@Override
	default <V> Supplier<V> thenApply(java.util.function.IntFunction<? extends V> after) {
		return Supplier.of(this, after);
	}

	@Override
	default DoubleSupplier thenApplyAsDouble(java.util.function.IntToDoubleFunction after) {
		return DoubleSupplier.of(this, after);
	}

	@Override
	default IntSupplier thenApplyAsInt(java.util.function.IntUnaryOperator after) {
		return IntSupplier.of(this, after);
	}

	@Override
	default LongSupplier thenApplyAsLong(java.util.function.IntToLongFunction after) {
		return LongSupplier.of(this, after);
	}

	@Override
	default Supplier<Integer> thenBox() {
		return this.thenApply(IntUnaryOperator.identity().thenBox());
	}

	@Override
	default BooleanSupplier thenTest(java.util.function.IntPredicate after) {
		return BooleanSupplier.of(this, after);
	}

	/**
	 * Returns a supplier of the specified value.
	 * @param value the supplied value
	 * @return a supplier of the specified value.
	 */
	static IntSupplier of(int value) {
		return new IntSupplier() {
			@Override
			public int getAsInt() {
				return value;
			}

			@Override
			public Supplier<Integer> thenBox() {
				return Supplier.of(Integer.valueOf(value));
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static IntSupplier of(Runnable before, java.util.function.IntSupplier after) {
		return new IntSupplier() {
			@Override
			public int getAsInt() {
				before.run();
				return after.getAsInt();
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
	static <T> IntSupplier of(java.util.function.Supplier<? extends T> before, java.util.function.ToIntFunction<? super T> after) {
		return new IntSupplier() {
			@Override
			public int getAsInt() {
				return after.applyAsInt(before.get());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static IntSupplier of(java.util.function.BooleanSupplier before, BooleanToIntFunction after) {
		return new IntSupplier() {
			@Override
			public int getAsInt() {
				return after.applyAsInt(before.getAsBoolean());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static IntSupplier of(java.util.function.DoubleSupplier before, java.util.function.DoubleToIntFunction after) {
		return new IntSupplier() {
			@Override
			public int getAsInt() {
				return after.applyAsInt(before.getAsDouble());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static IntSupplier of(java.util.function.IntSupplier before, java.util.function.IntUnaryOperator after) {
		return new IntSupplier() {
			@Override
			public int getAsInt() {
				return after.applyAsInt(before.getAsInt());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static IntSupplier of(java.util.function.LongSupplier before, java.util.function.LongToIntFunction after) {
		return new IntSupplier() {
			@Override
			public int getAsInt() {
				return after.applyAsInt(before.getAsLong());
			}
		};
	}
}
