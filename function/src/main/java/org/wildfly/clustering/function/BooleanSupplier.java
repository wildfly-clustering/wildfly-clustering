/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An enhanced boolean supplier.
 * @author Paul Ferraro
 */
public interface BooleanSupplier extends java.util.function.BooleanSupplier {
	BooleanSupplier TRUE = Boolean.TRUE::booleanValue;
	BooleanSupplier FALSE = Boolean.FALSE::booleanValue;

	/**
	 * Returns an if/else supplier that delegates to the first specified supplier when this supplier evaluates to true, or the second specified supplier when this supplier evaluates to false.
	 * @param <V> the mapped value type
	 * @param whenTrue the mapped supplier when evaluating to true
	 * @param whenFalse the mapped supplier when evaluating to false
	 * @return an if/else supplier that delegates to the first specified supplier when this supplier evaluates to true, or the second specified supplier when this supplier evaluates to false.
	 */
	default <V> Supplier<V> map(java.util.function.Supplier<V> whenTrue, java.util.function.Supplier<V> whenFalse) {
		return new Supplier<>() {
			@Override
			public V get() {
				return (BooleanSupplier.this.getAsBoolean() ? whenTrue : whenFalse).get();
			}
		};
	}

	/**
	 * Returns a boxed version of this supplier.
	 * @return a boxed version of this supplier.
	 */
	default Supplier<Boolean> boxed() {
		return map(Supplier.of(Boolean.TRUE), Supplier.of(Boolean.FALSE));
	}

	/**
	 * Returns a new supplier that delegates to this supplier using the specified exception handler.
	 * @param handler an exception handler
	 * @return a new supplier that delegates to this supplier using the specified exception handler.
	 */
	default BooleanSupplier handle(java.util.function.Predicate<RuntimeException> handler) {
		return new BooleanSupplier() {
			@Override
			public boolean getAsBoolean() {
				try {
					return BooleanSupplier.this.getAsBoolean();
				} catch (RuntimeException e) {
					return handler.test(e);
				}
			}
		};
	}

	/**
	 * Returns a supplier that always returns the specified value.
	 * @param value the supplied value
	 * @return a supplier that always returns the specified value.
	 */
	static BooleanSupplier of(boolean value) {
		return value ? TRUE : FALSE;
	}
}
