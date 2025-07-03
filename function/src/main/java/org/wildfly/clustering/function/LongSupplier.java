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
	LongSupplier ZERO = of(0L);

	/**
	 * Returns a supplier that returns the value this function mapped via the specified function.
	 * @param <V> the mapped value type
	 * @param mapper a mapping function
	 * @return a supplier that returns the value this function mapped via the specified function.
	 */
	default <V> Supplier<V> map(java.util.function.LongFunction<V> mapper) {
		return new Supplier<>() {
			@Override
			public V get() {
				return mapper.apply(LongSupplier.this.getAsLong());
			}
		};
	}

	/**
	 * Returns a boxed version of this supplier.
	 * @return a boxed version of this supplier.
	 */
	default Supplier<Long> boxed() {
		return map(Long::valueOf);
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

	static LongSupplier of(long value) {
		return new LongSupplier() {
			@Override
			public long getAsLong() {
				return value;
			}
		};
	}
}
