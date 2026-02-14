/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import java.util.Map;

/**
 * An operation with two parameters.
 * @author Paul Ferraro
 */
interface BinaryObjectOperation<V1, V2> {

	/**
	 * Composes a binary operation using the specified parameter mapping functions.
	 * @param <V1> the former mapped type
	 * @param <V2> the latter mapped type
	 * @param mapper1 a mapping function for the former parameter
	 * @param mapper2 a mapping function for the latter parameter
	 * @return a composed binary operation
	 */
	<T1, T2> BinaryObjectOperation<T1, T2> compose(java.util.function.Function<? super T1, ? extends V1> beforeFormer, java.util.function.Function<? super T2, ? extends V2> beforeLatter);

	/**
	 * Composes a unary operation using the specified parameter mapping functions.
	 * @param <V> the mapped type
	 * @param mapper1 a mapping function for the former parameter
	 * @param mapper2 a mapping function for the latter parameter
	 * @return a composed unary operation
	 */
	<T> ObjectOperation<T> composeUnary(java.util.function.Function<? super T, ? extends V1> beforeFormer, java.util.function.Function<? super T, ? extends V2> beforeLatter);

	/**
	 * Composes a unary operation using the specified parameter mapping functions.
	 * @param <V> the mapped type
	 * @param mapper1 a mapping function for the former parameter
	 * @param mapper2 a mapping function for the latter parameter
	 * @return a composed unary operation
	 */
	ObjectOperation<Map.Entry<V1, V2>> composeEntry();

	/**
	 * Returns a binary operation that with reversed parameter order.
	 * @return a binary operation that with reversed parameter order.
	 */
	BinaryObjectOperation<V2, V1> reverse();
}
