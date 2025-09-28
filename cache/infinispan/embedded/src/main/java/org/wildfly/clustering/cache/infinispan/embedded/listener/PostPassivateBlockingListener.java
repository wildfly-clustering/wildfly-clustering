/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import org.infinispan.Cache;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryPassivated;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryPassivatedEvent;

/**
 * Generic non-blocking post-passivation listener that delegates to a blocking consumer.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
@Listener(observation = Listener.Observation.POST)
public class PostPassivateBlockingListener<K, V> extends CacheEventListenerRegistrar<K, V> {

	private Consumer<CacheEntryEvent<K, V>> listener;

	/**
	 * Creates a blocking listener of post-passivate events.
	 * @param cache an embedded cache
	 * @param listener a consumer of post-passivate events
	 */
	public PostPassivateBlockingListener(Cache<K, V> cache, Consumer<K> listener) {
		super(cache);
		this.listener = new BlockingCacheEventListener<>(cache, listener);
	}

	/**
	 * Handles cache entry passivation events.
	 * @param event a cache entry passivation event
	 * @return a completion stage
	 */
	@CacheEntryPassivated
	public CompletionStage<Void> postPassivate(CacheEntryPassivatedEvent<K, V> event) {
		this.listener.accept(event);
		return CompletableFuture.completedStage(null);
	}
}
