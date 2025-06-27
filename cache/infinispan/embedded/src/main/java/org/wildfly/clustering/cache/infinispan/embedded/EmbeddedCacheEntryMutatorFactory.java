/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import org.infinispan.Cache;
import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.cache.CacheProperties;

/**
 * Factory for creating {@link CacheEntryMutator} objects for an Infinispan cache.
 * @param <K> the cache key type
 * @param <V> the cache value type
 * @author Paul Ferraro
 */
public class EmbeddedCacheEntryMutatorFactory<K, V> implements CacheEntryMutatorFactory<K, V> {

	private final Cache<K, V> cache;
	private final CacheProperties properties;

	EmbeddedCacheEntryMutatorFactory(Cache<K, V> cache) {
		this(cache, new EmbeddedCacheProperties(cache));
	}

	EmbeddedCacheEntryMutatorFactory(Cache<K, V> cache, CacheProperties properties) {
		this.cache = cache;
		this.properties = properties;
	}

	@Override
	public CacheEntryMutator createMutator(K key, V value) {
		return this.properties.isPersistent() ? new EmbeddedCacheEntryMutator<>(this.cache, key, value) : CacheEntryMutator.EMPTY;
	}
}
