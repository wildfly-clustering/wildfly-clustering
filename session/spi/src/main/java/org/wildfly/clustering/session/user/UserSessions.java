/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.user;

import java.util.Map;

/**
 * Represents an index of user sessions.
 * @author Paul Ferraro
 * @param <D> deployment identifier type
 * @param <S> session identifier type
 */
public interface UserSessions<D, S> extends AutoCloseable {
	/**
	 * Returns an immutable map of sessions per deployment for which the associated user is authenticated.
	 * @return a n immutable map of sessions per deployment.
	 */
	Map<D, S> getSessions();

	/**
	 * Returns the session of the specified deployment for which the associated user is authenticated.
	 * @param deployment a deployment
	 * @return the session of the user for the specified deployment, or null, if no session exists for the associated user.
	 */
	S getSession(D deployment);

	/**
	 * Removes and returns the session of the specified deployment for which the associated user is no longer authenticated.
	 * @param deployment a deployment
	 * @return the session of the specified deployment for which the associated user is no longer authenticated.
	 */
	S removeSession(D deployment);

	/**
	 * Adds the specified deployment and session identifiers to the set of deployments for which the associated user is authenticated.
	 * @param deployment a deployment identifier
	 * @param session a session identifier
	 * @return true, if the session was added, false it already exists
	 */
	boolean addSession(D deployment, S session);

	@Override
	void close();
}
