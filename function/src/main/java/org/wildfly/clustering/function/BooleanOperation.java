/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An operation on a boolean value.
 * @author Paul Ferraro
 */
interface BooleanOperation extends PrimitiveOperation<Boolean> {
	/**
	 * Composes an operation that invokes this operation using result of the specified operation.
	 * @param <V> the composed operation type
	 * @param before a composing operation
	 * @return an operation that invokes this operation using result of the specified operation.
	 */
	<V> ObjectOperation<V> compose(java.util.function.Predicate<? super V> before);

	/**
	 * Composes an operation that invokes this operation using result of the specified operation.
	 * @param <V1> the former composed operation type
	 * @param <V2> the latter composed operation type
	 * @param before a composing operation
	 * @return an operation that invokes this operation using result of the specified operation.
	 */
	<V1, V2> BinaryObjectOperation<V1, V2> composeBinary(java.util.function.BiPredicate<? super V1, ? super V2> before);

	/**
	 * Composes an operation that invokes this operation using result of the specified operation.
	 * @param before a composing operation
	 * @return an operation that invokes this operation using result of the specified operation.
	 */
	BooleanOperation composeBoolean(BooleanPredicate before);

	/**
	 * Composes an operation that invokes this operation using result of the specified operation.
	 * @param before a composing operation
	 * @return an operation that invokes this operation using result of the specified operation.
	 */
	DoubleOperation composeDouble(java.util.function.DoublePredicate before);

	/**
	 * Composes an operation that invokes this operation using result of the specified operation.
	 * @param before a composing operation
	 * @return an operation that invokes this operation using result of the specified operation.
	 */
	IntOperation composeInt(java.util.function.IntPredicate before);

	/**
	 * Composes an operation that invokes this operation using result of the specified operation.
	 * @param before a composing operation
	 * @return an operation that invokes this operation using result of the specified operation.
	 */
	LongOperation composeLong(java.util.function.LongPredicate before);
}
