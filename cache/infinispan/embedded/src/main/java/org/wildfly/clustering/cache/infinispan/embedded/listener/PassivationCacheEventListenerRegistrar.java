/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import java.util.List;

import org.infinispan.Cache;
import org.wildfly.clustering.function.BiConsumer;

/**
 * Registers listeners for pre-passivate and post-activate events.
 * Listener completion will require event consumption.
 * @author Paul Ferraro
 * @param <K> the cache key type
 * @param <V> the cache value type
 */
public class PassivationCacheEventListenerRegistrar<K, V> extends CacheEventListenerRegistrar<K, V> {

	/**
	 * Creates a listener of pre/post passivate/activate events.
	 * @param cache an embedded cache
	 * @param prePassivate a consumer of pre-passivate events
	 * @param postActivate a consumer of post-activate events
	 */
	public PassivationCacheEventListenerRegistrar(Cache<K, V> cache, BiConsumer<K, V> prePassivate, BiConsumer<K, V> postActivate) {
		super(cache, List.of(new PrePassivateListener<>(new BlockingCacheEntryEventListener<>(cache, prePassivate)), new PostActivateListener<>(new BlockingCacheEntryEventListener<>(cache, postActivate))));
	}
}
