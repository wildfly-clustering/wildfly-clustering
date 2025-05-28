/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.wildfly.clustering.function.BiFunction;

/**
 * A remote cache that performs locking reads.
 * @param <K> the cache key type
 * @param <V> the cache value type
 * @author Paul Ferraro
 */
public class ReadForUpdateRemoteCache<K, V> extends AbstractRemoteCache<K, V> {

	public ReadForUpdateRemoteCache(RemoteCache<K, V> cache) {
		super(cache);
	}

	@Override
	public CompletableFuture<V> getAsync(K key) {
		// Simulate a read operation using a write operation
		return this.withFlags(Flag.FORCE_RETURN_VALUE).computeIfPresentAsync(key, BiFunction.latter());
	}

	@Override
	public CompletableFuture<Map<K, V>> getAllAsync(Set<?> keys) {
		if (keys.isEmpty()) return CompletableFuture.completedFuture(Map.of());
		AtomicInteger remaining = new AtomicInteger(keys.size());
		Map<K, V> entries = new ConcurrentHashMap<>();
		CompletableFuture<Map<K, V>> result = new CompletableFuture<>();
		for (Object key : keys) {
			@SuppressWarnings("unchecked")
			K typedKey = (K) key;
			this.getAsync(typedKey).whenComplete((value, exception) -> {
				if (exception != null) {
					result.completeExceptionally(exception);
				} else {
					if (value != null) {
						entries.put(typedKey, value);
					}
					if (remaining.decrementAndGet() == 0) {
						result.complete(entries);
					}
				}
			});
		}
		return result;
	}

	@Override
	public RemoteCache<K, V> apply(RemoteCache<K, V> cache) {
		return new ReadForUpdateRemoteCache<>(cache);
	}
}
