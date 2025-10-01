/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.Collection;
import java.util.Set;

/**
 * Function that removes one or more elements from a set.
 * @author Paul Ferraro
 * @param <V> the set element type
 */
public class SetRemoveFunction<V> extends CollectionRemoveFunction<V, Set<V>> {

	/**
	 * Constructs a new function that removes the specified element from a set.
	 * @param value the value to be removed from the set
	 */
	public SetRemoveFunction(V value) {
		this(Set.of(value));
	}

	/**
	 * Constructs a new function that removes the specified elements from a set.
	 * @param values the values to be removed from the set
	 */
	public SetRemoveFunction(Collection<V> values) {
		super(values, SetOperations.forOperand(values.iterator().next()));
	}
}
