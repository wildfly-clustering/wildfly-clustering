/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * Enhanced {@link java.util.function.IntSupplier}.
 * @author Paul Ferraro
 */
public interface IntSupplier extends java.util.function.IntSupplier {
	IntSupplier ZERO = of(0);
	IntSupplier MINIMUM = of(Integer.MIN_VALUE);
	IntSupplier MAXIMUM = of(Integer.MAX_VALUE);

	/**
	 * Returns a runner that accepts the value returned by this supplier via the specified consumer.
	 * @param consumer a integer consumer
	 * @return a runner that accepts the value returned by this supplier via the specified consumer.
	 */
	default Runnable thenAccept(java.util.function.IntConsumer consumer) {
		return new Runnable() {
			@Override
			public void run() {
				consumer.accept(IntSupplier.this.getAsInt());
			}
		};
	}

	/**
	 * Returns a supplier that applies the specified function to the value returned by this supplier.
	 * @param <V> the mapped value type
	 * @param function a mapping function
	 * @return a supplier that applies the specified function to the value returned by this supplier.
	 */
	default <V> Supplier<V> thenApply(java.util.function.IntFunction<V> function) {
		return new Supplier<>() {
			@Override
			public V get() {
				return function.apply(IntSupplier.this.getAsInt());
			}
		};
	}

	/**
	 * Returns a supplier that applies the specified function to the value returned by this supplier.
	 * @param function a mapping function
	 * @return a supplier that applies the specified function to the value returned by this supplier.
	 */
	default DoubleSupplier thenApplyAsDouble(java.util.function.IntToDoubleFunction function) {
		return new DoubleSupplier() {
			@Override
			public double getAsDouble() {
				return function.applyAsDouble(IntSupplier.this.getAsInt());
			}
		};
	}

	/**
	 * Returns a supplier that applies the specified operator to the value returned by this supplier.
	 * @param operator a mapping operator
	 * @return a supplier that applies the specified operator to the value returned by this supplier.
	 */
	default IntSupplier thenApplyAsInt(java.util.function.IntUnaryOperator operator) {
		return new IntSupplier() {
			@Override
			public int getAsInt() {
				return operator.applyAsInt(IntSupplier.this.getAsInt());
			}
		};
	}

	/**
	 * Returns a supplier that applies the specified function to the value returned by this supplier.
	 * @param function a mapping function
	 * @return a supplier that applies the specified function to the value returned by this supplier.
	 */
	default LongSupplier thenApplyAsLong(java.util.function.IntToLongFunction function) {
		return new LongSupplier() {
			@Override
			public long getAsLong() {
				return function.applyAsLong(IntSupplier.this.getAsInt());
			}
		};
	}

	/**
	 * Returns a supplier that applies the specified predicate to the value returned by this supplier.
	 * @param predicate a integer predicate
	 * @return a supplier that applies the specified predicate to the value returned by this supplier.
	 */
	default BooleanSupplier thenTest(java.util.function.IntPredicate predicate) {
		return new BooleanSupplier() {
			@Override
			public boolean getAsBoolean() {
				return predicate.test(IntSupplier.this.getAsInt());
			}
		};
	}

	/**
	 * Returns a boxed version of this supplier.
	 * @return a boxed version of this supplier.
	 */
	default Supplier<Integer> boxed() {
		return thenApply(Integer::valueOf);
	}

	/**
	 * Returns a new supplier that delegates to this supplier using the specified exception handler.
	 * @param handler an exception handler
	 * @return a new supplier that delegates to this supplier using the specified exception handler.
	 */
	default IntSupplier handle(java.util.function.ToIntFunction<RuntimeException> handler) {
		return new IntSupplier() {
			@Override
			public int getAsInt() {
				try {
					return IntSupplier.this.getAsInt();
				} catch (RuntimeException e) {
					return handler.applyAsInt(e);
				}
			}
		};
	}

	static IntSupplier of(int value) {
		return new IntSupplier() {
			@Override
			public int getAsInt() {
				return value;
			}
		};
	}
}
