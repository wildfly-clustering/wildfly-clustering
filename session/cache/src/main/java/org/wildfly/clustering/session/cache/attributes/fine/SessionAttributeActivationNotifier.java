/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.fine;

/**
 * A notifier of passivation/activation events.
 * @author Paul Ferraro
 */
public interface SessionAttributeActivationNotifier extends AutoCloseable {

	/**
	 * Notifies the specified attribute that it will be passivated, if interested.
	 * @param value an attribute value
	 */
	void prePassivate(Object value);

	/**
	 * Notifies the specified attribute that it was activated, if interested.
	 * @param value an attribute value
	 */
	void postActivate(Object value);

	@Override
	void close();
}
