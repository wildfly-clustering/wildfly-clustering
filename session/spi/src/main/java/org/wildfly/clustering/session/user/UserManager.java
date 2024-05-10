/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.user;

import org.wildfly.clustering.server.manager.Manager;

/**
 * A user manager.
 * @author Paul Ferraro
 * @param <C> the user context type
 * @param <T> the transient user context type
 * @param <D> the deployment identifier type
 * @param <S> the session identifier type
 */
public interface UserManager<C, T, D, S> extends Manager<String> {
	/**
	 * Creates a new user with the specified identifier and context.
	 * @param id a unique user identifier
	 * @param context the user context
	 * @return a new user
	 */
	User<C, T, D, S> createUser(String id, C context);

	/**
	 * Returns the user identified by the specified identifier.
	 * @param id a unique user identifier
	 * @return an existing user, or null, if no user exists for the specified identifier.
	 */
	User<C, T, D, S> findUser(String id);
}
