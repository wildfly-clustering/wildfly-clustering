/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryPassivated;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryPassivatedEvent;

/**
 * Generic non-blocking pre-passivation listener that delegates to a generic cache event listener.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
@Listener(observation = Listener.Observation.PRE)
public class PrePassivateListener<K, V> {

	private final Function<CacheEntryEvent<K, V>, CompletionStage<Void>> listener;

	/**
	 * Creates a non-blocking pre-passivate listener
	 * @param listener a non-blocking listener function
	 */
	public PrePassivateListener(Function<CacheEntryEvent<K, V>, CompletionStage<Void>> listener) {
		this.listener = listener;
	}

	/**
	 * Handles cache entry passivation events.
	 * @param event a cache entry passivation event
	 * @return a completion stage
	 */
	@CacheEntryPassivated
	public CompletionStage<Void> prePassivate(CacheEntryPassivatedEvent<K, V> event) {
		return this.listener.apply(event);
	}
}
