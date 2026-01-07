/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An operation with an integer parameter.
 * @author Paul Ferraro
 */
interface IntOperation extends PrimitiveOperation<Integer> {
	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param <T> the mapped type
	 * @param before a composing function
	 * @return a composed consumer
	 */
	<T> ObjectOperation<T> compose(java.util.function.ToIntFunction<? super T> before);

	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param <T1> the former composition type
	 * @param <T1> the latter composition type
	 * @param before a composing function
	 * @return a consumer that invokes this consumer using result of the specified function.
	 */
	<T1, T2> BinaryObjectOperation<T1, T2> composeBinary(java.util.function.ToIntBiFunction<? super T1, ? super T2> before);

	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param before a composing function
	 * @return a consumer that invokes this consumer using result of the specified function.
	 */
	BooleanOperation composeBoolean(BooleanToIntFunction before);

	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param before a composing function
	 * @return a consumer that invokes this consumer using result of the specified function.
	 */
	DoubleOperation composeDouble(java.util.function.DoubleToIntFunction before);

	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param before a composing function
	 * @return a consumer that invokes this consumer using result of the specified function.
	 */
	IntOperation composeInt(java.util.function.IntUnaryOperator before);

	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param before a composing function
	 * @return a consumer that invokes this consumer using result of the specified function.
	 */
	LongOperation composeLong(java.util.function.LongToIntFunction before);
}
