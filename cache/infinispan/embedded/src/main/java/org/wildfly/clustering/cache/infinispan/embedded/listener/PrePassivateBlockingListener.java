/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import java.util.function.BiConsumer;

import org.infinispan.Cache;

/**
 * Generic non-blocking pre-passivation listener that delegates to a blocking consumer.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
public class PrePassivateBlockingListener<K, V> extends CacheEventListenerRegistrar<K, V> {

	/**
	 * Creates a blocking listener of pre-passivate events.
	 * @param cache an embedded cache
	 * @param listener a consumer of pre-passivate events
	 */
	public PrePassivateBlockingListener(Cache<K, V> cache, BiConsumer<K, V> listener) {
		super(cache, new PrePassivateListener<>(new BlockingCacheEventListener<>(cache, listener)));
	}
}
