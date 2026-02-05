/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.Map;

/**
 * Function that operates on a map.
 * @author Paul Ferraro
 * @param <K> the map key type
 * @param <V> the map value type
 * @param <T> the function operand type
 */
public abstract class AbstractMapOperationFunction<K, V, T> extends AbstractFunction<T, Map<K, V>> {

	/**
	 * Constructs a new map operation function
	 * @param operand the operation operand
	 * @param operations the map operations
	 */
	public AbstractMapOperationFunction(T operand, Operations<Map<K, V>> operations) {
		super(operand, operations.getCopier(), operations.getFactory(), operations.isEmpty());
	}
}
