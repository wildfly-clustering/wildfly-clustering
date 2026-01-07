/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An operation with a double parameter.
 * @author Paul Ferraro
 */
interface DoubleOperation extends PrimitiveOperation<Double> {
	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param <V> the mapped type
	 * @param before a composing function
	 * @return a composed consumer
	 */
	<T> ObjectOperation<T> compose(java.util.function.ToDoubleFunction<? super T> before);

	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param <T1> the former composition type
	 * @param <T1> the latter composition type
	 * @param before a composing function
	 * @return a consumer that invokes this consumer using result of the specified function.
	 */
	<T1, T2> BinaryObjectOperation<T1, T2> composeBinary(java.util.function.ToDoubleBiFunction<? super T1, ? super T2> before);

	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param before a composing function
	 * @return a consumer that invokes this consumer using result of the specified function.
	 */
	BooleanOperation composeBoolean(BooleanToDoubleFunction before);

	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param before a composing function
	 * @return a consumer that invokes this consumer using result of the specified function.
	 */
	DoubleOperation composeDouble(java.util.function.DoubleUnaryOperator before);

	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param before a composing function
	 * @return a consumer that invokes this consumer using result of the specified function.
	 */
	IntOperation composeInt(java.util.function.IntToDoubleFunction before);

	/**
	 * Composes a consumer that invokes this consumer using result of the specified function.
	 * @param before a composing function
	 * @return a consumer that invokes this consumer using result of the specified function.
	 */
	LongOperation composeLong(java.util.function.LongToDoubleFunction before);
}
