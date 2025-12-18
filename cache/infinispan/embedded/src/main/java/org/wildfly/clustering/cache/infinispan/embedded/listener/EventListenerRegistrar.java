/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import java.util.List;

import org.infinispan.notifications.Listenable;

/**
 * A registering Infinispan listener.
 * @author Paul Ferraro
 */
public class EventListenerRegistrar implements ListenerRegistrar {

	private final Listenable listenable;
	private final Iterable<Object> listeners;

	EventListenerRegistrar(Listenable listenable) {
		this.listenable = listenable;
		this.listeners = List.of(this);
	}

	/**
	 * Creates a registrar of event listeners.
	 * @param listenable the listener target
	 * @param listeners one or more listeners
	 */
	public EventListenerRegistrar(Listenable listenable, Iterable<Object> listeners) {
		this.listenable = listenable;
		this.listeners = listeners;
	}

	@Override
	public ListenerRegistration register() {
		for (Object listener : this.listeners) {
			this.listenable.addListener(listener);
		}
		return this.getListenerRegistration();
	}

	ListenerRegistration getListenerRegistration() {
		return () -> {
			for (Object listener : this.listeners) {
				this.listenable.removeListener(listener);
			}
		};
	}
}
