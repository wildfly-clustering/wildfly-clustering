/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import org.infinispan.client.hotrod.RemoteCache;
import org.wildfly.clustering.cache.infinispan.AbstractCacheEntryMutator;
import org.wildfly.clustering.function.Consumer;

/**
 * Mutates a given cache entry.
 * @param <K> the cache entry key type
 * @param <V> the cache entry value type
 * @author Paul Ferraro
 */
public class RemoteCacheEntryMutator<K, V> extends AbstractCacheEntryMutator {
	private final RemoteCache<K, V> cache;
	private final K key;
	private final V value;

	RemoteCacheEntryMutator(RemoteCache<K, V> cache, K key, V value) {
		this.cache = cache;
		this.key = key;
		this.value = value;
	}

	@Override
	public CompletionStage<Void> mutateAsync() {
		Duration maxIdleDuration = this.get();
		long seconds = maxIdleDuration.getSeconds();
		int nanos = maxIdleDuration.getNano();
		if (nanos > 0) {
			seconds += 1;
		}
		return this.cache.putAsync(this.key, this.value, 0, TimeUnit.SECONDS, seconds, TimeUnit.SECONDS).thenAccept(Consumer.empty());
	}
}
