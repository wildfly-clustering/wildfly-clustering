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

	/**
	 * Returns a boolean supplier that evaluates a predicate against a supplied value.
	 * @param predicate a predicate use to evaluate the supplied value
	 * @param supplier a supplier of the value to test
	 * @return a boolean supplier that evaluates a predicate against a supplied value.
	 */
	static <T> BooleanSupplier of(Predicate<T> predicate, Supplier<T> supplier) {
		return new BooleanSupplier() {
			@Override
			public boolean getAsBoolean() {
				return predicate.test(supplier.get());
			}
		};
	}

	/**
	 * Returns a boolean supplier that evaluates a predicate against a supplied value.
	 * @param predicate a predicate used to evaluate the supplied values
	 * @param formerSupplier a supplier of the former value to test
	 * @param latterSupplier a supplier of the latter value to test
	 * @return a boolean supplier that evaluates a predicate against a supplied value.
	 */
	static <T, U> BooleanSupplier of(BiPredicate<T, U> predicate, Supplier<T> formerSupplier, Supplier<U> latterSupplier) {
		return new BooleanSupplier() {
			@Override
			public boolean getAsBoolean() {
				return predicate.test(formerSupplier.get(), latterSupplier.get());
			}
		};
	}
}
