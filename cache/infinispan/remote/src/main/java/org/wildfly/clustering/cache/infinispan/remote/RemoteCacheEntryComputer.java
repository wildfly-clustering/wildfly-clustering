/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import org.infinispan.client.hotrod.RemoteCache;
import org.wildfly.clustering.cache.infinispan.AbstractCacheEntryMutator;
import org.wildfly.clustering.function.Consumer;

/**
 * Mutator for a cache entry using a compute function.
 * @author Paul Ferraro
 * @param <K> the cache key type
 * @param <V> the cache value type
 */
public class RemoteCacheEntryComputer<K, V> extends AbstractCacheEntryMutator {

	private final RemoteCache<K, V> cache;
	private final K key;
	private final BiFunction<Object, V, V> function;

	RemoteCacheEntryComputer(RemoteCache<K, V> cache, K key, BiFunction<Object, V, V> function) {
		this.cache = cache;
		this.key = key;
		this.function = function;
	}

	@Override
	public CompletionStage<Void> runAsync() {
		Duration maxIdleDuration = this.get();
		long seconds = maxIdleDuration.getSeconds();
		int nanos = maxIdleDuration.getNano();
		if (nanos > 0) {
			seconds += 1;
		}
		return this.cache.computeAsync(this.key, this.function, 0, TimeUnit.SECONDS, seconds, TimeUnit.SECONDS).thenAccept(Consumer.of());
	}
}
