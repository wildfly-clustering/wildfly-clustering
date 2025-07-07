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
	DoubleSupplier ZERO = of(0d);

	/**
	 * Returns a supplier that returns the value this function mapped via the specified function.
	 * @param <V> the mapped value type
	 * @param mapper a mapping function
	 * @return a supplier that returns the value this function mapped via the specified function.
	 */
	default <V> Supplier<V> map(java.util.function.DoubleFunction<V> mapper) {
		return new Supplier<>() {
			@Override
			public V get() {
				return mapper.apply(DoubleSupplier.this.getAsDouble());
			}
		};
	}

	/**
	 * Returns a boxed version of this supplier.
	 * @return a boxed version of this supplier.
	 */
	default Supplier<Double> boxed() {
		return map(Double::valueOf);
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

	static DoubleSupplier of(double value) {
		return new DoubleSupplier() {
			@Override
			public double getAsDouble() {
				return value;
			}
		};
	}
}
