/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.server.Registration;

/**
 * A factory for creating a session manager.
 * @param <C> the session manager context type
 * @param <SC> the session context type
 * @param <B> the batch type
 * @author Paul Ferraro
 */
public interface SessionManagerFactory<C, SC, B extends Batch> extends Registration {
	/**
	 * Create a session manager using the specified configuration.
	 * @param configuration a session manager configuration
	 * @return a new session manager
	 */
	SessionManager<SC, B> createSessionManager(SessionManagerConfiguration<C> configuration);
}
