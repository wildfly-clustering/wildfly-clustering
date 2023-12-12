/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.user;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.server.manager.Manager;

/**
 * The SSO equivalent of a session manager.
 * @author Paul Ferraro
 * @param <C> the user context type
 * @param <L> the local context type
 * @param <D> the deployment identifier type
 * @param <S> the session identifier type
 * @param <B> the batch type
 */
public interface UserManager<C, L, D, S, B extends Batch> extends Manager<String, B> {
	/**
	 * Creates a new single sign on entry.
	 * @param ssoId a unique SSO identifier
	 * @return a new SSO.
	 */
	User<C, L, D, S> createUser(String id, C context);

	/**
	 * Returns the single sign on entry identified by the specified identifier.
	 * @param ssoId a unique SSO identifier
	 * @return an existing SSO, or null, if no SSO was found
	 */
	User<C, L, D, S> findUser(String id);
}
