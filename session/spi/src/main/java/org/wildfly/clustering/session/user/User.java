/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.user;

/**
 * Represents a user, associated with one or more sessions.
 * @author Paul Ferraro
 * @param <C> the user context type
 * @param <L> the local context type
 * @param <D> the deployment identifier type
 * @param <S> the session identifier type
 */
public interface User<C, L, D, S> {
	/**
	 * Returns the unique identifier for this user.
	 * @return a unique identifier
	 */
	String getId();

	/**
	 * Returns the replicated context of this user.
	 * @return the user context.
	 */
	C getContext();

	/**
	 * The local context of this user.
	 * The local context is *not* replicated to other nodes in the cluster.
	 * @return a local context.
	 */
	L getLocalContext();

	/**
	 * Returns the sessions for this user.
	 * @return
	 */
	UserSessions<D, S> getSessions();

	/**
	 * Invalidates this user and any associated sessions.
	 */
	void invalidate();
}
