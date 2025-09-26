/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * Enhanced {@link java.util.function.IntSupplier}.
 * @author Paul Ferraro
 */
public interface DoubleSupplier extends java.util.function.DoubleSupplier {
	/** A supplier that always returns zero. */
	DoubleSupplier ZERO = of(0d);

	/**
	 * Returns a runner that accepts the value returned by this supplier via the specified consumer.
	 * @param consumer a integer consumer
	 * @return a runner that accepts the value returned by this supplier via the specified consumer.
	 */
	default Runnable thenAccept(java.util.function.DoubleConsumer consumer) {
		return new Runnable() {
			@Override
			public void run() {
				consumer.accept(DoubleSupplier.this.getAsDouble());
			}
		};
	}

	/**
	 * Returns a supplier that applies the specified function to the value returned by this supplier.
	 * @param <V> the mapped value type
	 * @param function a mapping function
	 * @return a supplier that applies the specified function to the value returned by this supplier.
	 */
	default <V> Supplier<V> thenApply(java.util.function.DoubleFunction<V> function) {
		return new Supplier<>() {
			@Override
			public V get() {
				return function.apply(DoubleSupplier.this.getAsDouble());
			}
		};
	}

	/**
	 * Returns a supplier that applies the specified operator to the value returned by this supplier.
	 * @param operator a mapping operator
	 * @return a supplier that applies the specified operator to the value returned by this supplier.
	 */
	default DoubleSupplier thenApplyAsDouble(java.util.function.DoubleUnaryOperator operator) {
		return new DoubleSupplier() {
			@Override
			public double getAsDouble() {
				return operator.applyAsDouble(DoubleSupplier.this.getAsDouble());
			}
		};
	}

	/**
	 * Returns a supplier that applies the specified function to the value returned by this supplier.
	 * @param function a mapping function
	 * @return a supplier that applies the specified function to the value returned by this supplier.
	 */
	default IntSupplier thenApplyAsInt(java.util.function.DoubleToIntFunction function) {
		return new IntSupplier() {
			@Override
			public int getAsInt() {
				return function.applyAsInt(DoubleSupplier.this.getAsDouble());
			}
		};
	}

	/**
	 * Returns a supplier that applies the specified function to the value returned by this supplier.
	 * @param function a mapping function
	 * @return a supplier that applies the specified function to the value returned by this supplier.
	 */
	default LongSupplier thenApplyAsLong(java.util.function.DoubleToLongFunction function) {
		return new LongSupplier() {
			@Override
			public long getAsLong() {
				return function.applyAsLong(DoubleSupplier.this.getAsDouble());
			}
		};
	}

	/**
	 * Returns a supplier that applies the specified predicate to the value returned by this supplier.
	 * @param predicate a predicate
	 * @return a supplier that applies the specified predicate to the value returned by this supplier.
	 */
	default BooleanSupplier thenTest(java.util.function.DoublePredicate predicate) {
		return new BooleanSupplier() {
			@Override
			public boolean getAsBoolean() {
				return predicate.test(DoubleSupplier.this.getAsDouble());
			}
		};
	}

	/**
	 * Returns a boxed version of this supplier.
	 * @return a boxed version of this supplier.
	 */
	default Supplier<Double> boxed() {
		return thenApply(Double::valueOf);
	}

	/**
	 * Returns a new supplier that delegates to this supplier using the specified exception handler.
	 * @param handler an exception handler
	 * @return a new supplier that delegates to this supplier using the specified exception handler.
	 */
	default DoubleSupplier handle(java.util.function.ToDoubleFunction<RuntimeException> handler) {
		return new DoubleSupplier() {
			@Override
			public double getAsDouble() {
				try {
					return DoubleSupplier.this.getAsDouble();
				} catch (RuntimeException e) {
					return handler.applyAsDouble(e);
				}
			}
		};
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
		};
	}
}
