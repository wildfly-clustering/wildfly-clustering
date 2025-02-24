/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.time.Duration;
import java.util.function.Function;

import org.infinispan.client.hotrod.RemoteCache;
import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;

/**
 * Factory for creating a {@link CacheEntryMutator} for a remote cache entry.
 * @param <K> the cache key type
 * @param <V> the cache value type
 * @author Paul Ferraro
 */
public class RemoteCacheEntryMutatorFactory<K, V> implements CacheEntryMutatorFactory<K, V> {

	private final RemoteCache<K, V> cache;
	private final Function<V, Duration> maxIdle;

	public RemoteCacheEntryMutatorFactory(RemoteCache<K, V> cache) {
		this(cache, null);
	}

	public RemoteCacheEntryMutatorFactory(RemoteCache<K, V> cache, Function<V, Duration> maxIdle) {
		this.cache = cache;
		this.maxIdle = maxIdle;
	}

	@Override
	public CacheEntryMutator createMutator(K key, V value) {
		return (this.maxIdle != null) ? new RemoteCacheEntryMutator<>(this.cache, key, value, () -> this.maxIdle.apply(value)) : new RemoteCacheEntryMutator<>(this.cache, key, value);
	}
}
