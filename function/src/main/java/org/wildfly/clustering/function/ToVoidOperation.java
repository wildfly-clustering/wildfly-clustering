/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An operation with no return value.
 * @author Paul Ferraro
 */
interface ToVoidOperation {

	/**
	 * Composes an operation that returns the value from the specified supplier after invoking this operation.
	 * @param after a supplier of the return value
	 * @return an operation that returns the value from the specified supplier after invoking this operation.
	 */
	<T> ToObjectOperation<T> thenReturn(java.util.function.Supplier<? extends T> after);

	/**
	 * Composes an operation that returns the value from the specified supplier after invoking this operation.
	 * @param after a supplier of the return value
	 * @return an operation that returns the value from the specified supplier after invoking this operation.
	 */
	ToBooleanOperation thenReturnBoolean(java.util.function.BooleanSupplier after);

	/**
	 * Composes an operation that returns the value from the specified supplier after invoking this operation.
	 * @param after a supplier of the return value
	 * @return an operation that returns the value from the specified supplier after invoking this operation.
	 */
	ToDoubleOperation thenReturnDouble(java.util.function.DoubleSupplier after);

	/**
	 * Composes an operation that returns the value from the specified supplier after invoking this operation.
	 * @param after a supplier of the return value
	 * @return an operation that returns the value from the specified supplier after invoking this operation.
	 */
	ToIntOperation thenReturnInt(java.util.function.IntSupplier after);

	/**
	 * Composes an operation that returns the value from the specified supplier after invoking this operation.
	 * @param after a supplier of the return value
	 * @return an operation that returns the value from the specified supplier after invoking this operation.
	 */
	ToLongOperation thenReturnLong(java.util.function.LongSupplier after);

	/**
	 * Composes an operation that runs the specified {@link Runnable} after this operation.
	 * @param after a runnable task to run after this operation
	 * @return an operation that runs the specified {@link Runnable} after this operation.
	 */
	ToVoidOperation thenRun(Runnable after);
}
