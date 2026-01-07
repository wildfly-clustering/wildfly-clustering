/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An operation accepting a single parameter.
 * @author Paul Ferraro
 */
interface ObjectOperation<V> {
	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param <T> the mapped type
	 * @param before a mapping function
	 * @return a mapped consumer
	 */
	<T> ObjectOperation<T> compose(java.util.function.Function<? super T, ? extends V> before);

	/**
	 * Composes an operation that invokes this operation using the result of the specified function.
	 * @param mapper a mapping function
	 * @param <T1> the former parameter type
	 * @param <T2> the latter parameter type
	 * @return a mapped operation
	 */
	<T1, T2> BinaryObjectOperation<T1, T2> composeBinary(java.util.function.BiFunction<? super T1, ? super T2, ? extends V> before);

	/**
	 * Composes an operation that invokes this operation using the result of the specified function.
	 * @param mapper a mapping function
	 * @return a mapped operation
	 */
	BooleanOperation composeBoolean(BooleanFunction<? extends V> before);

	/**
	 * Composes an operation that invokes this operation using the result of the specified function.
	 * @param mapper a mapping function
	 * @return a mapped operation
	 */
	DoubleOperation composeDouble(java.util.function.DoubleFunction<? extends V> before);

	/**
	 * Composes an operation that invokes this operation using the result of the specified function.
	 * @param mapper a mapping function
	 * @return a mapped operation
	 */
	IntOperation composeInt(java.util.function.IntFunction<? extends V> before);

	/**
	 * Composes an operation that invokes this operation using the result of the specified function.
	 * @param mapper a mapping function
	 * @return a mapped operation
	 */
	LongOperation composeLong(java.util.function.LongFunction<? extends V> before);
}
