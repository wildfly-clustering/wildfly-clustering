/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import org.infinispan.notifications.Listenable;

/**
 * A registering Infinispan listener.
 * @author Paul Ferraro
 */
public class EventListenerRegistrar implements ListenerRegistrar {

	private final Listenable listenable;
	private final Object listener;

	/**
	 * Creates a registrar of event listeners.
	 * @param listenable the listener target
	 */
	public EventListenerRegistrar(Listenable listenable) {
		this.listenable = listenable;
		this.listener = this;
	}

	/**
	 * Creates a registrar of event listeners.
	 * @param listenable the listener target
	 * @param listener a listener
	 */
	public EventListenerRegistrar(Listenable listenable, Object listener) {
		this.listenable = listenable;
		this.listener = listener;
	}

	@Override
	public ListenerRegistration register() {
		this.listenable.addListener(this.listener);
		return () -> this.listenable.removeListener(this.listener);
	}
}
