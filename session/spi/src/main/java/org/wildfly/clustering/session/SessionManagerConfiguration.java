/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session;

import org.wildfly.clustering.server.expiration.ExpirationConfiguration;
import org.wildfly.clustering.server.manager.ManagerConfiguration;

/**
 * Encapsulates the configuration of a session manager.
 * @author Paul Ferraro
 * @param <C> the session manager context type
 */
public interface SessionManagerConfiguration<C> extends ManagerConfiguration<String>, ExpirationConfiguration<ImmutableSession> {
	/**
	 * Returns the container-specific context of this session manager.
	 * @return a container-specific session manager context
	 */
	C getContext();
}
