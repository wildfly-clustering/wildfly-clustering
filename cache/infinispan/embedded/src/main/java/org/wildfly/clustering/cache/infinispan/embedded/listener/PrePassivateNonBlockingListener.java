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

	public PrePassivateNonBlockingListener(Cache<K, V> cache, BiConsumer<K, V> consumer) {
		super(cache, new PrePassivateListener<>(new NonBlockingCacheEventListener<>(consumer)));
	}
}
