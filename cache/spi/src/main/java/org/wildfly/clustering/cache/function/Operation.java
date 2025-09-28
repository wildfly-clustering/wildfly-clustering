/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

/**
 * Encapsulates an operation.
 * @author Paul Ferraro
 * @param <O> the operand type
 */
public interface Operation<O> {

	/**
	 * Returns the operand of the operation.
	 * @return the operand of the operation.
	 */
	O getOperand();
}
