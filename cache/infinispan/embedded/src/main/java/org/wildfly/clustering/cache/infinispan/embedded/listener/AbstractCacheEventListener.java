/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import org.infinispan.notifications.FilteringListenable;

/**
 * A self-registering event listener.
 * @author Paul Ferraro
 * @param <K> the cache key
 * @param <V> the cache value
 */
public abstract class AbstractCacheEventListener<K, V> extends CacheEventListenerRegistrar<K, V> {

	/**
	 * Creates a registrar for a cache event listener
	 * @param listenable a listener target
	 */
	protected AbstractCacheEventListener(FilteringListenable<K, V> listenable) {
		super(listenable);
	}
}
