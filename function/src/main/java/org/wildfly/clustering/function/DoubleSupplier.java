/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A supplier of a double value.
 * @author Paul Ferraro
 */
public interface DoubleSupplier extends java.util.function.DoubleSupplier, PrimitiveSupplier<Double>, ToDoubleOperation {
	/** A supplier that always returns zero. */
	DoubleSupplier ZERO = of(0d);

	@Override
	default DoubleSupplier compose(Runnable before) {
		return DoubleSupplier.of(before, this);
	}

	@Override
	default <T> ToDoubleFunction<T> compose(java.util.function.Consumer<? super T> before) {
		return ToDoubleFunction.of(before, this);
	}

	@Override
	default <T1, T2> ToDoubleBiFunction<T1, T2> composeBinary(java.util.function.BiConsumer<? super T1, ? super T2> before) {
		return ToDoubleBiFunction.of(before, this);
	}

	@Override
	default BooleanToDoubleFunction composeBoolean(BooleanConsumer before) {
		return BooleanToDoubleFunction.of(before, this);
	}

	@Override
	default DoubleUnaryOperator composeDouble(java.util.function.DoubleConsumer before) {
		return DoubleUnaryOperator.of(before, this);
	}

	@Override
	default IntToDoubleFunction composeInt(java.util.function.IntConsumer before) {
		return IntToDoubleFunction.of(before, this);
	}

	@Override
	default LongToDoubleFunction composeLong(java.util.function.LongConsumer before) {
		return LongToDoubleFunction.of(before, this);
	}

	@Override
	default Runner thenAccept(java.util.function.DoubleConsumer after) {
		return Runner.of(this, after);
	}

	@Override
	default <V> Supplier<V> thenApply(java.util.function.DoubleFunction<? extends V> after) {
		return Supplier.of(this, after);
	}

	@Override
	default DoubleSupplier thenApplyAsDouble(java.util.function.DoubleUnaryOperator after) {
		return DoubleSupplier.of(this, after);
	}

	@Override
	default IntSupplier thenApplyAsInt(java.util.function.DoubleToIntFunction after) {
		return IntSupplier.of(this, after);
	}

	@Override
	default LongSupplier thenApplyAsLong(java.util.function.DoubleToLongFunction after) {
		return LongSupplier.of(this, after);
	}

	@Override
	default Supplier<Double> thenBox() {
		return this.thenApply(DoubleUnaryOperator.identity().thenBox());
	}

	@Override
	default BooleanSupplier thenTest(java.util.function.DoublePredicate after) {
		return BooleanSupplier.of(this, after);
	}

	/**
	 * Returns a supplier of the specified value.
	 * @param value the supplied value
	 * @return a supplier of the specified value.
	 */
	static DoubleSupplier of(double value) {
		return new DoubleSupplier() {
			@Override
			public double getAsDouble() {
				return value;
			}

			@Override
			public Supplier<Double> thenBox() {
				return Supplier.of(Double.valueOf(value));
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static DoubleSupplier of(Runnable before, java.util.function.DoubleSupplier after) {
		return new DoubleSupplier() {
			@Override
			public double getAsDouble() {
				before.run();
				return after.getAsDouble();
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
	static <T> DoubleSupplier of(java.util.function.Supplier<? extends T> before, java.util.function.ToDoubleFunction<? super T> after) {
		return new DoubleSupplier() {
			@Override
			public double getAsDouble() {
				return after.applyAsDouble(before.get());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static DoubleSupplier of(java.util.function.BooleanSupplier before, BooleanToDoubleFunction after) {
		return new DoubleSupplier() {
			@Override
			public double getAsDouble() {
				return after.applyAsDouble(before.getAsBoolean());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static DoubleSupplier of(java.util.function.DoubleSupplier before, java.util.function.DoubleUnaryOperator after) {
		return new DoubleSupplier() {
			@Override
			public double getAsDouble() {
				return after.applyAsDouble(before.getAsDouble());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static DoubleSupplier of(java.util.function.IntSupplier before, java.util.function.IntToDoubleFunction after) {
		return new DoubleSupplier() {
			@Override
			public double getAsDouble() {
				return after.applyAsDouble(before.getAsInt());
			}
		};
	}

	/**
	 * Composes a supplier from the specified operations.
	 * @param before the former operation
	 * @param after the latter operation
	 * @return a composite supplier
	 */
	static DoubleSupplier of(java.util.function.LongSupplier before, java.util.function.LongToDoubleFunction after) {
		return new DoubleSupplier() {
			@Override
			public double getAsDouble() {
				return after.applyAsDouble(before.getAsLong());
			}
		};
	}
}
