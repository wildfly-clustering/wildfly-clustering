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
	/** A supplier that always returns true */
	BooleanSupplier TRUE = Boolean.TRUE::booleanValue;
	/** A supplier that always returns false */
	BooleanSupplier FALSE = Boolean.FALSE::booleanValue;

	/**
	 * Returns a supplier that return this negation of this supplier.
	 * @return a supplier that return this negation of this supplier.
	 */
	default BooleanSupplier negate() {
		if (this == TRUE) return FALSE;
		if (this == FALSE) return TRUE;
		return new BooleanSupplier() {
			@Override
			public boolean getAsBoolean() {
				return !BooleanSupplier.this.getAsBoolean();
			}
		};
	}

	/**
	 * Returns a boxed version of this supplier.
	 * @return a boxed version of this supplier.
	 */
	default Supplier<Boolean> boxed() {
		return new Supplier<>() {
			@Override
			public Boolean get() {
				return BooleanSupplier.this.getAsBoolean() ? Boolean.TRUE : Boolean.FALSE;
			}
		};
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
