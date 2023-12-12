/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan;

import java.util.Objects;

import org.wildfly.clustering.cache.Key;

/**
 * A base cache key implementation.
 * @author Paul Ferraro
 */
public class CacheKey<K> implements Key<K> {
	private final K id;

	public CacheKey(K id) {
		this.id = id;
	}

	@Override
	public K getId() {
		return this.id;
	}

	@Override
	public boolean equals(Object object) {
		if ((object == null) || (object.getClass() != this.getClass())) return false;
		@SuppressWarnings("unchecked")
		CacheKey<K> key = (CacheKey<K>) object;
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
