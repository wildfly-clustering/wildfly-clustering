/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function with a primitive value.
 * @author Paul Ferraro
 * @param <B> the boxed type
 * @param <P> the parameter type
 */
interface PrimitiveFunction<B, V> extends PrimitiveOperation<B>, ToObjectOperation<V> {

	@Override
	Function<B, V> box();
}
