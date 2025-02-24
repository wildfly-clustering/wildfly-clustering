/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.infinispan.client.hotrod.RemoteCache;
import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.common.function.Functions;

/**
 * Mutator for a cache entry using a compute function.
 * @author Paul Ferraro
 * @param <K> the cache key type
 * @param <V> the cache value type
 */
public class RemoteCacheEntryComputer<K, V> implements CacheEntryMutator {

	private final RemoteCache<K, V> cache;
	private final K key;
	private final BiFunction<Object, V, V> function;
	private final Supplier<Duration> maxIdle;

	public RemoteCacheEntryComputer(RemoteCache<K, V> cache, K key, BiFunction<Object, V, V> function) {
		this(cache, key, function, Functions.constantSupplier(Duration.ZERO));
	}

	public RemoteCacheEntryComputer(RemoteCache<K, V> cache, K key, BiFunction<Object, V, V> function, Supplier<Duration> maxIdle) {
		this.cache = cache;
		this.key = key;
		this.function = function;
		this.maxIdle = maxIdle;
	}

	@Override
	public CompletionStage<Void> mutateAsync() {
		Duration maxIdleDuration = this.maxIdle.get();
		long seconds = maxIdleDuration.getSeconds();
		int nanos = maxIdleDuration.getNano();
		if (nanos > 0) {
			seconds += 1;
		}
		return this.cache.computeAsync(this.key, this.function, 0, TimeUnit.SECONDS, seconds, TimeUnit.SECONDS).thenAccept(Functions.discardingConsumer());
	}
}
