/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import org.infinispan.Cache;
import org.infinispan.commons.executors.BlockingResource;
import org.infinispan.commons.executors.NonBlockingResource;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;
import org.wildfly.clustering.function.BiConsumer;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Runner;
import org.wildfly.clustering.function.Supplier;

/**
 * Generic event listener whose completion requires event consumption.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
public class BlockingCacheEntryEventListener<K, V> extends AbstractCacheEntryEventListener<K, V> {
	private static final Executor DIRECT = Runnable::run;

	/**
	 * Creates a blocking cache event listener.
	 * @param cache the target cache
	 * @param consumer a consumer of a cache key
	 */
	public BlockingCacheEntryEventListener(Cache<K, V> cache, Consumer<K> consumer) {
		super(cache, consumer);
	}

	/**
	 * Creates a blocking cache event listener.
	 * @param cache the target cache
	 * @param consumer a consumer of a cache entry
	 */
	public BlockingCacheEntryEventListener(Cache<K, V> cache, BiConsumer<K, V> consumer) {
		super(cache, consumer);
	}

	@Override
	public CompletionStage<Void> apply(CacheEntryEvent<K, V> event) {
		ThreadGroup group = Thread.currentThread().getThreadGroup();
		// Execute directly if current thread allows blocking operations
		Executor executor = (group instanceof BlockingResource) ? DIRECT : this.getBlockingExecutor();
		Runnable notification = Supplier.of(event).thenAccept(this.getConsumer());
		CompletionStage<Void> stage = CompletableFuture.runAsync(notification, executor);
		// Subscribe on non-blocking thread, if current thread was non-blocking
		return (group instanceof NonBlockingResource) ? stage.thenRunAsync(Runner.of(), this.getNonBlockingExecutor()) : stage;
	}
}
