/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * A function with two primitive parameters.
 * @author Paul Ferraro
 * @param <B> the boxed primitive type
 */
interface BinaryPrimitiveOperation<B> {
	/**
	 * Returns an operation that accepts a boxed primitive.
	 * @return an operation that accepts a boxed primitive.
	 */
	BinaryObjectOperation<B, B> box();

	/**
	 * Returns a binary operation that with reversed parameter order.
	 * @return a binary operation that with reversed parameter order.
	 */
	BinaryPrimitiveOperation<B> reverse();
}
