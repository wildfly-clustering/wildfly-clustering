/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import java.util.concurrent.CompletionStage;

import org.infinispan.Cache;
import org.infinispan.commons.util.concurrent.CompletableFutures;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;
import org.wildfly.clustering.function.BiConsumer;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Supplier;

/**
 * Generic event listener whose completion is not dependent on event consumption.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
public class NonBlockingCacheEntryEventListener<K, V> extends AbstractCacheEntryEventListener<K, V> {

	/**
	 * Creates a non-blocking cache event listener.
	 * @param cache the target cache
	 * @param consumer a consumer of a cache key
	 */
	public NonBlockingCacheEntryEventListener(Cache<K, V> cache, Consumer<K> consumer) {
		super(cache, consumer);
	}

	/**
	 * Creates a non-blocking cache event listener.
	 * @param cache the target cache
	 * @param consumer a consumer of a cache entry
	 */
	public NonBlockingCacheEntryEventListener(Cache<K, V> cache, BiConsumer<K, V> consumer) {
		super(cache, consumer);
	}

	@Override
	public CompletionStage<Void> apply(CacheEntryEvent<K, V> event) {
		// Fire and forget via blocking executor
		this.getBlockingExecutor().execute(Supplier.of(event).thenAccept(this.getConsumer()));
		return CompletableFutures.completedNull();
	}
}
