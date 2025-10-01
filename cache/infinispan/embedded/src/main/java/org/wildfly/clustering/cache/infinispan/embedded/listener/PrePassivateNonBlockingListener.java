/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import java.util.function.BiConsumer;

import org.infinispan.Cache;

/**
 * Generic non-blocking pre-passivation listener that delegates to a non-blocking consumer.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
public class PrePassivateNonBlockingListener<K, V> extends CacheEventListenerRegistrar<K, V> {

	/**
	 * Creates a non-blocking pre-passivate listener
	 * @param cache an embedded cache
	 * @param listener a non-blocking listener function
	 */
	public PrePassivateNonBlockingListener(Cache<K, V> cache, BiConsumer<K, V> listener) {
		super(cache, new PrePassivateListener<>(new NonBlockingCacheEventListener<>(listener)));
	}
}
