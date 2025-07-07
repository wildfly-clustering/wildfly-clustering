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

	/**
	 * Returns a supplier that returns the value this function mapped via the specified function.
	 * @param <V> the mapped value type
	 * @param mapper a mapping function
	 * @return a supplier that returns the value this function mapped via the specified function.
	 */
	default <V> Supplier<V> map(java.util.function.IntFunction<V> mapper) {
		return new Supplier<>() {
			@Override
			public V get() {
				return mapper.apply(IntSupplier.this.getAsInt());
			}
		};
	}

	/**
	 * Returns a boxed version of this supplier.
	 * @return a boxed version of this supplier.
	 */
	default Supplier<Integer> boxed() {
		return map(Integer::valueOf);
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
