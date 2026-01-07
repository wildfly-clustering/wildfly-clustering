/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function returning a primitive type.
 * @author Paul Ferraro
 * @param <P> the function parameter type
 * @param <B> the boxed primitive type
 * @param <F> the function type
 */
interface ToPrimitiveFunction<V, B> extends ObjectOperation<V>, ToPrimitiveOperation<B> {

	@Override
	Function<V, B> thenBox();
}
