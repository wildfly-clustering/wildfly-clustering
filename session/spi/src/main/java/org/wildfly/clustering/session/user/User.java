/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.user;

/**
 * Represents a user, associated with one or more sessions.
 * @author Paul Ferraro
 * @param <C> the user context type
 * @param <T> the transient context type
 * @param <D> the deployment identifier type
 * @param <S> the session identifier type
 */
public interface User<C, T, D, S> {
	/**
	 * Returns the unique identifier for this user.
	 * @return a unique identifier
	 */
	String getId();

	/**
	 * Returns the persistent context of this user.
	 * @return the persistent user context.
	 */
	C getPersistentContext();

	/**
	 * Returns the transient context of this user.
	 * @return the transient user context
	 */
	T getTransientContext();

	/**
	 * Returns the sessions for this user.
	 * @return the sessions for this user.
	 */
	UserSessions<D, S> getSessions();

	/**
	 * Invalidates this user and any associated sessions.
	 */
	void invalidate();
}
