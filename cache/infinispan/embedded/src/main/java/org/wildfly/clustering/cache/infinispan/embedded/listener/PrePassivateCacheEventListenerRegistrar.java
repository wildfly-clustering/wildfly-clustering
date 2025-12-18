/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import org.infinispan.Cache;
import org.wildfly.clustering.function.BiConsumer;

/**
 * Pre-passivation listener whose completion requires event consumption.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
public class PrePassivateCacheEventListenerRegistrar<K, V> extends CacheEventListenerRegistrar<K, V> {

	/**
	 * Creates a blocking listener of pre-passivate events.
	 * @param cache an embedded cache
	 * @param listener a consumer of pre-passivate events
	 */
	public PrePassivateCacheEventListenerRegistrar(Cache<K, V> cache, BiConsumer<K, V> listener) {
		super(cache, new PrePassivateListener<>(new BlockingCacheEntryEventListener<>(cache, listener)));
	}
}
