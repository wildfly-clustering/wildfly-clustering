/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * Enhanced {@link java.util.function.LongSupplier}.
 * @author Paul Ferraro
 */
public interface LongSupplier extends java.util.function.LongSupplier, PrimitiveSupplier<Long>, ToLongOperation {
	/** A supplier that always returns {@value Long#MIN_VALUE}. */
	LongSupplier MINIMUM = of(Long.MIN_VALUE);
	/** A supplier that always returns zero. */
	LongSupplier ZERO = of(0L);
	/** A supplier that always returns {@value Long#MAX_VALUE}. */
	LongSupplier MAXIMUM = of(Long.MAX_VALUE);

	@Override
	default LongSupplier compose(Runnable before) {
		return LongSupplier.of(before, this);
	}

	@Override
	default <T> ToLongFunction<T> compose(java.util.function.Consumer<? super T> before) {
		return ToLongFunction.of(before, this);
	}

	@Override
	default <T1, T2> ToLongBiFunction<T1, T2> composeBinary(java.util.function.BiConsumer<? super T1, ? super T2> before) {
		return ToLongBiFunction.of(before, this);
	}

	@Override
	default BooleanToLongFunction composeBoolean(BooleanConsumer before) {
		return BooleanToLongFunction.of(before, this);
	}

	@Override
	default DoubleToLongFunction composeDouble(java.util.function.DoubleConsumer before) {
		return DoubleToLongFunction.of(before, this);
	}

	@Override
	default IntToLongFunction composeInt(java.util.function.IntConsumer before) {
		return IntToLongFunction.of(before, this);
	}

	@Override
	default LongUnaryOperator composeLong(java.util.function.LongConsumer before) {
		return LongUnaryOperator.of(before, this);
	}

	@Override
	default Runner thenAccept(java.util.function.LongConsumer after) {
		return Runner.of(this, after);
	}

	@Override
	default <V> Supplier<V> thenApply(java.util.function.LongFunction<? extends V> after) {
		return Supplier.of(this, after);
	}

	@Override
	default DoubleSupplier thenApplyAsDouble(java.util.function.LongToDoubleFunction after) {
		return DoubleSupplier.of(this, after);
	}

	@Override
	default IntSupplier thenApplyAsInt(java.util.function.LongToIntFunction after) {
		return IntSupplier.of(this, after);
	}

	@Override
	default LongSupplier thenApplyAsLong(java.util.function.LongUnaryOperator after) {
		return LongSupplier.of(this, after);
	}

	@Override
	default Supplier<Long> thenBox() {
		return this.thenApply(LongUnaryOperator.identity().thenBox());
	}

	@Override
	default BooleanSupplier thenTest(java.util.function.LongPredicate after) {
		return BooleanSupplier.of(this, after);
	}

	/**
	 * Returns a supplier of the specified value.
	 * @param value the supplied value
	 * @return a supplier of the specified value.
	 */
	static LongSupplier of(long value) {
		return new LongSupplier() {
			@Override
			public long getAsLong() {
				return value;
			}

			@Override
			public Supplier<Long> thenBox() {
				return Supplier.of(Long.valueOf(value));
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static LongSupplier of(Runnable before, java.util.function.LongSupplier after) {
		return new LongSupplier() {
			@Override
			public long getAsLong() {
				before.run();
				return after.getAsLong();
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
	static <T> LongSupplier of(java.util.function.Supplier<? extends T> before, java.util.function.ToLongFunction<? super T> after) {
		return new LongSupplier() {
			@Override
			public long getAsLong() {
				return after.applyAsLong(before.get());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static LongSupplier of(java.util.function.BooleanSupplier before, BooleanToLongFunction after) {
		return new LongSupplier() {
			@Override
			public long getAsLong() {
				return after.applyAsLong(before.getAsBoolean());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static LongSupplier of(java.util.function.DoubleSupplier before, java.util.function.DoubleToLongFunction after) {
		return new LongSupplier() {
			@Override
			public long getAsLong() {
				return after.applyAsLong(before.getAsDouble());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static LongSupplier of(java.util.function.IntSupplier before, java.util.function.IntToLongFunction after) {
		return new LongSupplier() {
			@Override
			public long getAsLong() {
				return after.applyAsLong(before.getAsInt());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static LongSupplier of(java.util.function.LongSupplier before, java.util.function.LongUnaryOperator after) {
		return new LongSupplier() {
			@Override
			public long getAsLong() {
				return after.applyAsLong(before.getAsLong());
			}
		};
	}
}
