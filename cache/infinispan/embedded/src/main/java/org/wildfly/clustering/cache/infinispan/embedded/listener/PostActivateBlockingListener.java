/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.infinispan.Cache;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryActivated;
import org.infinispan.notifications.cachelistener.event.CacheEntryActivatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;

/**
 * Generic non-blocking post-activation listener that delegates to a blocking consumer.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
@Listener(observation = Listener.Observation.POST)
public class PostActivateBlockingListener<K, V> extends CacheEventListenerRegistrar<K, V> {

	private final Function<CacheEntryEvent<K, V>, CompletionStage<Void>> listener;

	/**
	 * Creates a blocking listener of post-activate events.
	 * @param cache an embedded cache
	 * @param listener a consumer of post-activate events
	 */
	public PostActivateBlockingListener(Cache<K, V> cache, BiConsumer<K, V> listener) {
		super(cache);
		this.listener = new BlockingCacheEventListener<>(cache, listener);
	}

	/**
	 * Handles cache entry activation events.
	 * @param event a cache entry activation event
	 * @return a completion stage
	 */
	@CacheEntryActivated
	public CompletionStage<Void> postActivate(CacheEntryActivatedEvent<K, V> event) {
		return this.listener.apply(event);
	}
}
