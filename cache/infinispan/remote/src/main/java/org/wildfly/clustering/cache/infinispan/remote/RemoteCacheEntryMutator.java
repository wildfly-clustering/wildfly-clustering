/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import org.infinispan.client.hotrod.RemoteCache;
import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Supplier;

/**
 * Mutates a given cache entry.
 * @param <K> the cache entry key type
 * @param <V> the cache entry value type
 * @author Paul Ferraro
 */
public class RemoteCacheEntryMutator<K, V> implements CacheEntryMutator {

	private final RemoteCache<K, V> cache;
	private final K id;
	private final V value;
	private final java.util.function.Supplier<Duration> maxIdle;

	public RemoteCacheEntryMutator(RemoteCache<K, V> cache, K id, V value) {
		this(cache, id, value, Supplier.of(Duration.ZERO));
	}

	public RemoteCacheEntryMutator(RemoteCache<K, V> cache, K id, V value, java.util.function.Supplier<Duration> maxIdle) {
		this.cache = cache;
		this.id = id;
		this.value = value;
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
		return this.cache.putAsync(this.id, this.value, 0, TimeUnit.SECONDS, seconds, TimeUnit.SECONDS).thenAccept(Consumer.empty());
	}
}
