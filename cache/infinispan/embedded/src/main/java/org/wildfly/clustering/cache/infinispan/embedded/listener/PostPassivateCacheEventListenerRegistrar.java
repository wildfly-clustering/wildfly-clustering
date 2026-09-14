/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import org.infinispan.Cache;
import org.wildfly.clustering.function.Consumer;

/**
 * A post-passivation cache event listener.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
public class PostPassivateCacheEventListenerRegistrar<K, V> extends CacheEventListenerRegistrar<K, V> {
	/**
	 * Creates a blocking listener of post-passivate events.
	 * @param cache an embedded cache
	 * @param listener a consumer of post-passivate events
	 */
	public PostPassivateCacheEventListenerRegistrar(Cache<K, V> cache, Consumer<K> listener) {
		super(cache, new PostPassivateListener<>(new NonBlockingCacheEntryEventListener<>(cache, listener)));
	}
}
