/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import org.infinispan.notifications.FilteringListenable;
import org.infinispan.notifications.cachelistener.filter.CacheEventFilter;

/**
 * A registering cache event listener.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
public class CacheEventListenerRegistrar<K, V> extends EventListenerRegistrar implements CacheListenerRegistrar<K, V> {

	private final FilteringListenable<K, V> listenable;
	private final Object listener;

	public CacheEventListenerRegistrar(FilteringListenable<K, V> listenable) {
		super(listenable);
		this.listenable = listenable;
		this.listener = this;
	}

	public CacheEventListenerRegistrar(FilteringListenable<K, V> listenable, Object listener) {
		super(listenable, listener);
		this.listenable = listenable;
		this.listener = listener;
	}

	@Override
	public ListenerRegistration register(CacheEventFilter<? super K, ? super V> filter) {
		this.listenable.addListener(this.listener, filter, null);
		return () -> this.listenable.removeListener(this.listener);
	}
}
