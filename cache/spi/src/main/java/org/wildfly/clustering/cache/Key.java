/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache;

/**
 * A cache key for a given identifier
 * @param <I> the identifier type of the cache key
 * @author Paul Ferraro
 */
public interface Key<I> {
	/**
	 * Returns the unique identifier of this key.
	 * @return the unique identifier of this key
	 */
	I getId();
}
