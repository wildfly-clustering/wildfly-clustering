/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.user;

import java.util.Set;

/**
 * Represents an index of user sessions.
 * @author Paul Ferraro
 * @param <D> deployment identifier type
 * @param <S> session identifier type
 */
public interface UserSessions<D, S> {
	/**
	 * Returns the set of deployments for which the associated user is authenticated.
	 * @return a set of deployment identifiers.
	 */
	Set<D> getDeployments();

	/**
	 * Returns the corresponding session identifier for the specified deployment.
	 * @param deployment a deployment identifier
	 * @return the session identifier of the user for the specified deployment, or null, if no session exists for the associated user.
	 */
	S getSession(D deployment);

	/**
	 * Removes the specified deployment from the set of deployments for which the associated user is authenticated.
	 * @param deployment a deployment identifier
	 * @return the session identifier of the user for the specified deployment, or null, if no session exists for the associated user.
	 */
	S removeSession(D deployment);

	/**
	 * Adds the specified deployment and session identifiers to the set of deployments for which the associated user is authenticated.
	 * @param deployment a deployment identifier
	 * @param session a session identifier
	 * @return true, if the session was added, false it already exists
	 */
	boolean addSession(D deployment, S session);
}
