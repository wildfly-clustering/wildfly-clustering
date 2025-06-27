/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.function.Supplier;

/**
 * Supplies a runtime exception.
 * @author Paul Ferraro
 * @param <E> the exception type
 */
public interface RuntimeExceptionSupplier<E extends RuntimeException> extends Supplier<E> {
	/**
	 * Returns a supplier that always returns the specified exception.
	 * @param <E> the exception type
	 * @param exception the supplied exception
	 * @return a supplier that always returns the specified exception.
	 */
	static <E extends RuntimeException> RuntimeExceptionSupplier<E> of(E exception) {
		return new RuntimeExceptionSupplier<>() {
			@Override
			public E get() {
				return exception;
			}
		};
	}

	/**
	 * Returns a supplier of an arbitrary type that throws the supplied exception.
	 * @param <T> the supplied type, though never returned
	 * @return a supplier of an arbitrary type that throws the supplied exception.
	 */
	default <T> Supplier<T> thenThrow() {
		return new Supplier<>() {
			@Override
			public T get() {
				throw RuntimeExceptionSupplier.this.get();
			}
		};
	}
}
