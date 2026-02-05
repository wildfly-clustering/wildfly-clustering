/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.Collection;

/**
 * Function that adds one or more elements to a collection.
 * @author Paul Ferraro
 * @param <V> the collection element type
 * @param <C> the collection type
 */
public class CollectionAddFunction<V, C extends Collection<V>> extends AbstractCollectionOperationFunction<V, C> {

	/**
	 * Constructs a new function that adds the specified elements to a collection.
	 * @param values the elements to be added to the collection
	 * @param operations the operations
	 */
	public CollectionAddFunction(Collection<V> values, Operations<C> operations) {
		super(values, operations);
	}

	@Override
	public void accept(C collection, Collection<V> operand) {
		collection.addAll(operand);
	}
}
