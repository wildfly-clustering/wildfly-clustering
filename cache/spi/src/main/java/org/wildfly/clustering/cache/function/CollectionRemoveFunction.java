/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.Collection;

/**
 * Function that removes one or more elements from a collection.
 * @author Paul Ferraro
 * @param <V> the collection element type
 * @param <C> the collection type
 */
public class CollectionRemoveFunction<V, C extends Collection<V>> extends CollectionFunction<V, C> {

	/**
	 * Constructs a new function that removes the specified elements from a collection.
	 * @param values the elements to be removed from the collection
	 * @param operations the operations
	 */
	public CollectionRemoveFunction(Collection<V> values, Operations<C> operations) {
		super(values, operations);
	}

	@Override
	public void accept(C collection, Collection<V> operand) {
		collection.removeAll(operand);
	}
}
