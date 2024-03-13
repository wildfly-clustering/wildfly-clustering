/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.util.Objects;
import java.util.function.Function;

/**
 * A function with 3 parameters.
 * @param <P1> the first function parameter type
 * @param <P2> the second function parameter type
 * @param <P3> the third function parameter type
 * @param <R> the function result type
 * @author Paul Ferraro
 */
public interface TriFunction<P1, P2, P3, R> {

	/**
	 * Applies this function to the given parameters.
	 * @param p1 the first function parameter
	 * @param p2 the second function parameter
	 * @param p3 the third function parameter
	 * @return the function result
	 */
	R apply(P1 p1, P2 p2, P3 p3);

	/**
	 * Returns a composed function that first applies this function to its input, and then applies the {@code after} function to the result.
	 * If evaluation of either function throws an exception, it is relayed to the caller of the composed function.
	 *
	 * @param <V> the type of output of the {@code after} function, and of the composed function
	 * @param after the function to apply after this function is applied
	 * @return a composed function that first applies this function and then applies the {@code after} function
	 * @throws NullPointerException if after is null
	 */
	default <V> TriFunction<P1, P2, P3, V> andThen(Function<? super R, ? extends V> after) {
		Objects.requireNonNull(after);
		return (p1, p2, p3) -> after.apply(this.apply(p1, p2, p3));
	}
}
