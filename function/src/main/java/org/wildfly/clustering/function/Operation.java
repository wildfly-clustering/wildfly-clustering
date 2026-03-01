/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

/**
 * An operation.
 * @author Paul Ferraro
 */
interface Operation {
	/**
	 * Composes an operation that runs the specified {@link Runnable} before this operation.
	 * @param before a runnable task to run before this operation
	 * @return an operation that runs the specified {@link Runnable} before this operation.
	 */
	Operation compose(Runnable before);

	/**
	 * Composes an operation that runs the specified {@link Runnable} after this operation.
	 * @param after a runnable task to run after this operation
	 * @return an operation that runs the specified {@link Runnable} after this operation.
	 */
	Operation thenRun(Runnable after);
}
