/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;
import org.wildfly.clustering.function.BiConsumer;
import org.wildfly.clustering.function.Consumer;

/**
 * Generic non-blocking event listener that delegates to a non-blocking event consumer.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
public class NonBlockingCacheEventListener<K, V> implements Function<CacheEntryEvent<K, V>, CompletionStage<Void>>, java.util.function.Consumer<CacheEntryEvent<K, V>> {

	private final java.util.function.BiConsumer<K, V> consumer;

	/**
	 * Creates a blocking cache event listener.
	 * @param consumer a consumer for a given event
	 */
	public NonBlockingCacheEventListener(java.util.function.Consumer<K> consumer) {
		this(BiConsumer.of(consumer, Consumer.empty()));
	}

	/**
	 * Creates a blocking cache event listener.
	 * @param consumer a consumer for a given event
	 */
	public NonBlockingCacheEventListener(java.util.function.BiConsumer<K, V> consumer) {
		this.consumer = consumer;
	}

	@Override
	public CompletionStage<Void> apply(CacheEntryEvent<K, V> event) {
		try {
			this.accept(event);
			return CompletableFuture.completedStage(null);
		} catch (RuntimeException | Error e) {
			return CompletableFuture.failedStage(e);
		}
	}

	@Override
	public void accept(CacheEntryEvent<K, V> event) {
		this.consumer.accept(event.getKey(), event.getValue());
	}
}
