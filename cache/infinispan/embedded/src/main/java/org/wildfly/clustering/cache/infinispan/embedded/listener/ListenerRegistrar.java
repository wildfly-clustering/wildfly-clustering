/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.listener;

/**
 * A registering Infinispan listener.
 * @author Paul Ferraro
 */
public interface ListenerRegistrar {

	/**
	 * Registers this listener for a Infinispan events.
	 * @return a listener registration
	 */
	ListenerRegistration register();
}
