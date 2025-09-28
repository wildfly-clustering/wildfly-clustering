/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan;

import java.util.Objects;

import org.wildfly.clustering.cache.Key;

/**
 * An base implementation for Infinispan cache keys.
 * @param <I> the identifier type of this cache key.
 * @author Paul Ferraro
 */
public class CacheKey<I> implements Key<I> {
	private final I id;

	/**
	 * Creates a cache key using the specified identifier.
	 * @param id the identifier of this key.
	 */
	public CacheKey(I id) {
		this.id = id;
	}

	@Override
	public I getId() {
		return this.id;
	}

	@Override
	public boolean equals(Object object) {
		if ((object == null) || (object.getClass() != this.getClass())) return false;
		@SuppressWarnings("unchecked")
		CacheKey<I> key = (CacheKey<I>) object;
		return this.id.equals(key.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getClass().getName(), this.id);
	}

	@Override
	public String toString() {
		return String.format("%s(%s)", this.getClass().getSimpleName(), this.id.toString());
	}
}
