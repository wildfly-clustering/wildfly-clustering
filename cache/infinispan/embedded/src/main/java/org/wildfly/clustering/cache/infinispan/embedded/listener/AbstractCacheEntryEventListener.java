/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import org.infinispan.Cache;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;
import org.infinispan.util.concurrent.BlockingManager;
import org.wildfly.clustering.function.BiConsumer;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;

/**
 * A generic cache entry event listener, with completion-requiring and set-and-forget methods.
 * @author Paul Ferraro
 * @param <K> the cache key
 * @param <V> the cache value
 */
public abstract class AbstractCacheEntryEventListener<K, V> implements Function<CacheEntryEvent<K, V>, CompletionStage<Void>> {
	private final Consumer<CacheEntryEvent<K, V>> consumer;
	private final Executor blockingExecutor;
	private final Executor nonBlockingExecutor;

	/**
	 * Creates a blocking cache event listener.
	 * @param cache the target cache
	 * @param consumer a consumer of a cache key
	 */
	protected AbstractCacheEntryEventListener(Cache<K, V> cache, Consumer<K> consumer) {
		this(cache, consumer.compose(CacheEntryEvent::getKey), consumer.getClass());
	}

	/**
	 * Creates a blocking cache event listener.
	 * @param cache the target cache
	 * @param consumer a consumer of a cache entry
	 */
	protected AbstractCacheEntryEventListener(Cache<K, V> cache, BiConsumer<K, V> consumer) {
		this(cache, consumer.composeUnary(CacheEntryEvent::getKey, CacheEntryEvent::getValue), consumer.getClass());
	}

	private AbstractCacheEntryEventListener(Cache<K, V> cache, Consumer<CacheEntryEvent<K, V>> consumer, Class<?> consumerClass) {
		this.consumer = consumer;
		BlockingManager manager = GlobalComponentRegistry.componentOf(cache.getCacheManager(), BlockingManager.class);
		this.blockingExecutor = manager.asExecutor(consumerClass.getName());
		this.nonBlockingExecutor = manager.nonBlockingExecutor();
	}

	Executor getBlockingExecutor() {
		return this.blockingExecutor;
	}

	Executor getNonBlockingExecutor() {
		return this.nonBlockingExecutor;
	}

	Consumer<CacheEntryEvent<K, V>> getConsumer() {
		return this.consumer;
	}
}
