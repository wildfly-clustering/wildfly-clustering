/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An operation returning a primitive type.
 * @author Paul Ferraro
 * @param <B> the boxed primitive type
 */
interface ToPrimitiveOperation<B> {
	/**
	 * Returns a boxed version of this operation.
	 * @return a boxed version of this operation.
	 */
	ToObjectOperation<B> thenBox();
}
