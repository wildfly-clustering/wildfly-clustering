/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session;

/**
 * A factory for creating a session manager.
 * @param <DC> the deployment context type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public interface SessionManagerFactory<DC, SC> extends AutoCloseable {
	/**
	 * Create a session manager using the specified configuration.
	 * @param configuration a session manager configuration
	 * @return a new session manager
	 */
	SessionManager<SC> createSessionManager(SessionManagerConfiguration<DC> configuration);

	@Override
	void close();
}
