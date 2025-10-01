/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache.function;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Defines operations for creating and copying an operable collection.
 * @param <V> the collection element type
 * @param <C> the collection type
 * @author Paul Ferraro
 */
public interface CollectionOperations<V, C extends Collection<V>> extends Operations<C> {

	@Override
	default Predicate<C> isEmpty() {
		return Collection::isEmpty;
	}
}
