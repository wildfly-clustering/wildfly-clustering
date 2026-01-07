/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * @author Paul Ferraro
 */
interface MappableToObjectOperation<T> {

	/**
	 * Returns an operation that applies the specified function to the result of this operation.
	 * @param after a function applied to the result of this operation
	 * @return an operation that applies the specified function to the result of this operation.
	 */
	<R> MappableToObjectOperation<R> thenApply(java.util.function.Function<? super T, ? extends R> after);
}
