/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import java.util.concurrent.CompletionStage;

import org.infinispan.Cache;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;
import org.infinispan.util.concurrent.BlockingManager;
import org.wildfly.clustering.function.BiConsumer;
import org.wildfly.clustering.function.Consumer;

/**
 * Generic non-blocking event listener that delegates to a blocking event consumer.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
public class BlockingCacheEventListener<K, V> extends NonBlockingCacheEventListener<K, V> {

	private final BlockingManager blocking;
	private final String name;

	/**
	 * Creates a blocking cache event listener.
	 * @param cache the target cache
	 * @param consumer a consumer of a cache key
	 */
	public BlockingCacheEventListener(Cache<K, V> cache, java.util.function.Consumer<K> consumer) {
		this(cache, BiConsumer.of(consumer, Consumer.empty()), consumer.getClass());
	}

	/**
	 * Creates a blocking cache event listener.
	 * @param cache the target cache
	 * @param consumer a consumer of a cache entry
	 */
	public BlockingCacheEventListener(Cache<K, V> cache, java.util.function.BiConsumer<K, V> consumer) {
		this(cache, consumer, consumer.getClass());
	}

	private BlockingCacheEventListener(Cache<K, V> cache, java.util.function.BiConsumer<K, V> consumer, Class<?> consumerClass) {
		super(consumer);
		this.blocking = GlobalComponentRegistry.componentOf(cache.getCacheManager(), BlockingManager.class);
		this.name = consumerClass.getName();
	}

	@Override
	public CompletionStage<Void> apply(CacheEntryEvent<K, V> event) {
		return this.blocking.runBlocking(() -> super.accept(event), this.name);
	}

	@Override
	public void accept(CacheEntryEvent<K, V> event) {
		this.blocking.asExecutor(this.name).execute(() -> super.accept(event));
	}
}
