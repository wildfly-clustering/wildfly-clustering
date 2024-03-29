/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.fine;

import java.util.function.BiConsumer;

/**
 * @author Paul Ferraro
 */
public interface SessionAttributeActivationNotifier extends AutoCloseable {
	BiConsumer<SessionAttributeActivationNotifier, Object> PRE_PASSIVATE = SessionAttributeActivationNotifier::prePassivate;
	BiConsumer<SessionAttributeActivationNotifier, Object> POST_ACTIVATE = SessionAttributeActivationNotifier::postActivate;

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
