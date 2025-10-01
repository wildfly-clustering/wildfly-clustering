/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.Collection;
import java.util.Set;

/**
 * Function that adds one or more items to a set.
 * @author Paul Ferraro
 * @param <V> the set element type
 */
public class SetAddFunction<V> extends CollectionAddFunction<V, Set<V>> {

	/**
	 * Constructs a new function that adds the specified element to a set.
	 * @param value the value to be added to the set
	 */
	public SetAddFunction(V value) {
		this(Set.of(value));
	}

	/**
	 * Constructs a new function that adds the specified elements to a set.
	 * @param values the values to be added to the set
	 */
	public SetAddFunction(Collection<V> values) {
		super(values, SetOperations.forOperand(values.iterator().next()));
	}
}
