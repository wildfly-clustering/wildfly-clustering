/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container;

import org.wildfly.clustering.session.Session;

/**
 * A mock passivation listener.
 * @author Paul Ferraro
 * @param <C> the session context type
 */
public interface PassivationListener<C> {

	void passivated(Session<C> session);

	void activated(Session<C> session);
}
