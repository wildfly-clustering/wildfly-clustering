/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session;

import org.wildfly.clustering.server.Registration;

/**
 * A factory for creating a session manager.
 * @param <C> the session manager context type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public interface SessionManagerFactory<C, SC> extends Registration {
	/**
	 * Create a session manager using the specified configuration.
	 * @param configuration a session manager configuration
	 * @return a new session manager
	 */
	SessionManager<SC> createSessionManager(SessionManagerConfiguration<C> configuration);
}
