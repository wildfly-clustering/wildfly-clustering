/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import java.util.concurrent.CompletionStage;

import org.infinispan.Cache;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryPassivated;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryPassivatedEvent;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;

/**
 * A post-passivation cache event listener.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
@Listener(observation = Listener.Observation.POST)
public class PostPassivateCacheEventListenerRegistrar<K, V> extends CacheEventListenerRegistrar<K, V> {
	private static final System.Logger LOGGER = System.getLogger(PrePassivateCacheEventListenerRegistrar.class.getName());

	private final Function<CacheEntryEvent<K, V>, CompletionStage<Void>> listener;

	/**
	 * Creates a blocking listener of post-passivate events.
	 * @param cache an embedded cache
	 * @param listener a consumer of post-passivate events
	 */
	public PostPassivateCacheEventListenerRegistrar(Cache<K, V> cache, Consumer<K> listener) {
		super(cache);
		// Fire-and-forget via blocking executor
		this.listener = new NonBlockingCacheEntryEventListener<>(cache, listener);
	}

	/**
	 * Handles cache entry passivation events.
	 * @param event a cache entry passivation event
	 * @return a completion stage
	 */
	@CacheEntryPassivated
	public CompletionStage<Void> prePassivate(CacheEntryPassivatedEvent<K, V> event) {
		LOGGER.log(System.Logger.Level.TRACE, "Cache {0} received post-passivate event for {1}", event.getCache().getName(), event.getKey());
		return this.listener.apply(event);
	}
}
