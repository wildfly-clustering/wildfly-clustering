/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An operation with a primitive parameter.
 * @author Paul Ferraro
 * @param <B> the boxed type
 */
interface PrimitiveOperation<B> {

	/**
	 * Returns a boxed version of this operation.
	 * @return a boxed version of this operation.
	 */
	ObjectOperation<B> box();
}
