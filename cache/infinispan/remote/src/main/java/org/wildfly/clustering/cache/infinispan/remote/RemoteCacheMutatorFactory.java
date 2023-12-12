/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.time.Duration;
import java.util.function.Function;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;

/**
 * Factory for creating a {@link CacheEntryMutator} for a remote cache entry.
 * @author Paul Ferraro
 */
public class RemoteCacheMutatorFactory<K, V> implements CacheEntryMutatorFactory<K, V> {

	private final RemoteCache<K, V> cache;
	private final Flag[] flags;
	private final Function<V, Duration> maxIdle;

	public RemoteCacheMutatorFactory(RemoteCache<K, V> cache, Flag[] flags) {
		this(cache, flags, null);
	}

	public RemoteCacheMutatorFactory(RemoteCache<K, V> cache, Flag[] flags, Function<V, Duration> maxIdle) {
		this.cache = cache;
		this.flags = flags;
		this.maxIdle = maxIdle;
	}

	@Override
	public CacheEntryMutator createMutator(K key, V value) {
		return (this.maxIdle != null) ? new RemoteCacheEntryMutator<>(this.cache, this.flags, key, value, () -> this.maxIdle.apply(value)) : new RemoteCacheEntryMutator<>(this.cache, this.flags, key, value);
	}
}
