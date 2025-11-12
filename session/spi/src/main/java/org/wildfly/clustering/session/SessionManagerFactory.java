/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session;

/**
 * A factory for creating a session manager.
 * @param <CC> the container context type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public interface SessionManagerFactory<CC, SC> extends AutoCloseable {
	/**
	 * Create a session manager using the specified configuration.
	 * @param configuration a session manager configuration
	 * @return a new session manager
	 */
	SessionManager<SC> createSessionManager(SessionManagerConfiguration<CC> configuration);

	@Override
	void close();
}
