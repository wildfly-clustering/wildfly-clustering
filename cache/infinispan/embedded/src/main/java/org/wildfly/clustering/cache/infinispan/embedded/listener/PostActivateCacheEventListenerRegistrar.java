/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import org.infinispan.Cache;
import org.wildfly.clustering.function.BiConsumer;

/**
 * Generic non-blocking post-activation listener that delegates to a blocking consumer.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
public class PostActivateCacheEventListenerRegistrar<K, V> extends CacheEventListenerRegistrar<K, V> {

	/**
	 * Creates a blocking listener of post-activate events.
	 * @param cache an embedded cache
	 * @param listener a consumer of post-activate events
	 */
	public PostActivateCacheEventListenerRegistrar(Cache<K, V> cache, BiConsumer<K, V> listener) {
		super(cache, new PostActivateListener<>(new BlockingCacheEntryEventListener<>(cache, listener)));
	}
}
