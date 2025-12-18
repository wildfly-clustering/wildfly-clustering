/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

import org.infinispan.notifications.Listenable;

/**
 * A self-registering event listener.
 * @author Paul Ferraro
 */
public abstract class AbstractEventListener extends EventListenerRegistrar {

	/**
	 * Creates a self-registering event listener
	 * @param listenable the listener target
	 */
	protected AbstractEventListener(Listenable listenable) {
		super(listenable);
	}
}
