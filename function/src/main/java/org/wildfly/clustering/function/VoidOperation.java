/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An operation with no parameters.
 * @author Paul Ferraro
 */
interface VoidOperation {
	/**
	 * Composes an operation that runs the specified {@link Runnable} before this operation.
	 * @param before a runnable task to run before this operation
	 * @return an operation that runs the specified {@link Runnable} before this operation.
	 */
	VoidOperation compose(Runnable before);

	/**
	 * Composes an operation that consumes an object before invoking this operation.
	 * @param before a consumer to accept a value before invoking this operation
	 * @return an operation that consumes an object before invoking this operation.
	 */
	<T> ObjectOperation<T> compose(java.util.function.Consumer<? super T> before);

	/**
	 * Composes an operation that consumes an object before invoking this operation.
	 * @param before a consumer to accept a value before invoking this operation
	 * @return an operation that consumes an object before invoking this operation.
	 */
	<T1, T2> BinaryObjectOperation<T1, T2> composeBinary(java.util.function.BiConsumer<? super T1, ? super T2> before);

	/**
	 * Composes an operation that consumes an object before invoking this operation.
	 * @param before a consumer to accept a value before invoking this operation
	 * @return an operation that consumes an object before invoking this operation.
	 */
	BooleanOperation composeBoolean(BooleanConsumer before);

	/**
	 * Composes an operation that consumes an object before invoking this operation.
	 * @param before a consumer to accept a value before invoking this operation
	 * @return an operation that consumes an object before invoking this operation.
	 */
	DoubleOperation composeDouble(java.util.function.DoubleConsumer before);

	/**
	 * Composes an operation that consumes an object before invoking this operation.
	 * @param before a consumer to accept a value before invoking this operation
	 * @return an operation that consumes an object before invoking this operation.
	 */
	IntOperation composeInt(java.util.function.IntConsumer before);

	/**
	 * Composes an operation that consumes an object before invoking this operation.
	 * @param before a consumer to accept a value before invoking this operation
	 * @return an operation that consumes an object before invoking this operation.
	 */
	LongOperation composeLong(java.util.function.LongConsumer before);
}
