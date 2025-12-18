/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import java.util.concurrent.CompletionStage;

import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryActivated;
import org.infinispan.notifications.cachelistener.event.CacheEntryActivatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;
import org.wildfly.clustering.function.Function;

/**
 * Generic non-blocking pre-passivation listener that delegates to a generic cache event listener.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
@Listener(observation = Listener.Observation.POST)
public class PostActivateListener<K, V> {
	private static final System.Logger LOGGER = System.getLogger(PostActivateCacheEventListenerRegistrar.class.getName());

	private final Function<CacheEntryEvent<K, V>, CompletionStage<Void>> listener;

	/**
	 * Creates a non-blocking pre-passivate listener
	 * @param listener a non-blocking listener function
	 */
	public PostActivateListener(Function<CacheEntryEvent<K, V>, CompletionStage<Void>> listener) {
		this.listener = listener;
	}

	/**
	 * Handles cache entry activation events.
	 * @param event a cache entry activation event
	 * @return a completion stage
	 */
	@CacheEntryActivated
	public CompletionStage<Void> postActivate(CacheEntryActivatedEvent<K, V> event) {
		LOGGER.log(System.Logger.Level.TRACE, "Cache {0} received post-activate event for {1}", event.getCache().getName(), event.getKey());
		return this.listener.apply(event);
	}
}
