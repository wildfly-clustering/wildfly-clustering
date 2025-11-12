/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * Enhanced {@link java.util.function.LongSupplier}.
 * @author Paul Ferraro
 */
public interface LongSupplier extends java.util.function.LongSupplier {
	/** A supplier that always returns {@value Long#MIN_VALUE}. */
	LongSupplier MINIMUM = of(Long.MIN_VALUE);
	/** A supplier that always returns zero. */
	LongSupplier ZERO = of(0L);
	/** A supplier that always returns {@value Long#MAX_VALUE}. */
	LongSupplier MAXIMUM = of(Long.MAX_VALUE);

	/**
	 * Returns a runner that accepts the value returned by this supplier via the specified consumer.
	 * @param consumer a integer consumer
	 * @return a runner that accepts the value returned by this supplier via the specified consumer.
	 */
	default Runner thenAccept(java.util.function.LongConsumer consumer) {
		return Runner.accept(consumer, this);
	}

	/**
	 * Returns a supplier that applies the specified function to the value returned by this supplier.
	 * @param <V> the mapped value type
	 * @param function a mapping function
	 * @return a supplier that applies the specified function to the value returned by this supplier.
	 */
	default <V> Supplier<V> thenApply(java.util.function.LongFunction<V> function) {
		return new Supplier<>() {
			@Override
			public V get() {
				return function.apply(LongSupplier.this.getAsLong());
			}
		};
	}

	/**
	 * Returns a supplier that applies the specified function to the value returned by this supplier.
	 * @param function a mapping function
	 * @return a supplier that applies the specified function to the value returned by this supplier.
	 */
	default DoubleSupplier thenApplyAsDouble(java.util.function.LongToDoubleFunction function) {
		return new DoubleSupplier() {
			@Override
			public double getAsDouble() {
				return function.applyAsDouble(LongSupplier.this.getAsLong());
			}
		};
	}

	/**
	 * Returns a supplier that applies the specified function to the value returned by this supplier.
	 * @param function a mapping function
	 * @return a supplier that applies the specified function to the value returned by this supplier.
	 */
	default IntSupplier thenApplyAsInt(java.util.function.LongToIntFunction function) {
		return new IntSupplier() {
			@Override
			public int getAsInt() {
				return function.applyAsInt(LongSupplier.this.getAsLong());
			}
		};
	}

	/**
	 * Returns a supplier that applies the specified operator to the value returned by this supplier.
	 * @param operator a mapping operator
	 * @return a supplier that applies the specified operator to the value returned by this supplier.
	 */
	default LongSupplier thenApplyAsLong(java.util.function.LongUnaryOperator operator) {
		return new LongSupplier() {
			@Override
			public long getAsLong() {
				return operator.applyAsLong(LongSupplier.this.getAsLong());
			}
		};
	}

	/**
	 * Returns a supplier that applies the specified predicate to the value returned by this supplier.
	 * @param predicate a predicate
	 * @return a supplier that applies the specified predicate to the value returned by this supplier.
	 */
	default BooleanSupplier thenTest(java.util.function.LongPredicate predicate) {
		return new BooleanSupplier() {
			@Override
			public boolean getAsBoolean() {
				return predicate.test(LongSupplier.this.getAsLong());
			}
		};
	}

	/**
	 * Returns a boxed version of this supplier.
	 * @return a boxed version of this supplier.
	 */
	default Supplier<Long> boxed() {
		return thenApply(Long::valueOf);
	}

	/**
	 * Returns a new supplier that delegates to this supplier using the specified exception handler.
	 * @param handler an exception handler
	 * @return a new supplier that delegates to this supplier using the specified exception handler.
	 */
	default LongSupplier handle(java.util.function.ToLongFunction<RuntimeException> handler) {
		return new LongSupplier() {
			@Override
			public long getAsLong() {
				try {
					return LongSupplier.this.getAsLong();
				} catch (RuntimeException e) {
					return handler.applyAsLong(e);
				}
			}
		};
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
		};
	}
}
