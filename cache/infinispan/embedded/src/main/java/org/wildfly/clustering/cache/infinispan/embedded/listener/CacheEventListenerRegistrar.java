/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import java.util.List;

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
	private final Iterable<Object> listeners;

	CacheEventListenerRegistrar(FilteringListenable<K, V> listenable) {
		super(listenable);
		this.listenable = listenable;
		this.listeners = List.of(this);
	}

	/**
	 * Creates a registrar for a cache event listener
	 * @param listenable a listener target
	 * @param listener a cache event listener
	 */
	public CacheEventListenerRegistrar(FilteringListenable<K, V> listenable, Object listener) {
		this(listenable, List.of(listener));
	}

	/**
	 * Creates a registrar for a cache event listener
	 * @param listenable a listener target
	 * @param listeners one or more cache event listeners
	 */
	public CacheEventListenerRegistrar(FilteringListenable<K, V> listenable, Iterable<Object> listeners) {
		super(listenable, listeners);
		this.listenable = listenable;
		this.listeners = listeners;
	}

	@Override
	public ListenerRegistration register(CacheEventFilter<? super K, ? super V> filter) {
		for (Object listener : this.listeners) {
			this.listenable.addListener(listener, filter, null);
		}
		return this.getListenerRegistration();
	}
}
