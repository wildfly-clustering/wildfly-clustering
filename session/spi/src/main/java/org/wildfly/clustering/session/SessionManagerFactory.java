/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.server.Registration;

/**
 * A factory for creating a session manager.
 * @param <DC> the deployment context type
 * @param <SC> the session context type
 * @param <B> the batch type
 * @author Paul Ferraro
 */
public interface SessionManagerFactory<DC, SC, B extends Batch> extends Registration {
	/**
	 * Create as session manager using the specified context and identifier factory.
	 * @param context a session context
	 * @param idFactory a session identifier factory
	 * @return a new session manager
	 */
	SessionManager<SC, B> createSessionManager(SessionManagerConfiguration<DC, B> configuration);
}
