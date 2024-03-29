/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session;

import java.time.Instant;

import org.wildfly.clustering.server.expiration.ExpirationMetaData;

/**
 * Abstraction for immutable meta information about a session.
 * @author Paul Ferraro
 */
public interface ImmutableSessionMetaData extends ExpirationMetaData {

	/**
	 * Indicates whether or not this session was created by the current thread.
	 * @return true, if this session is new, false otherwise
	 */
	default boolean isNew() {
		return this.getLastAccessStartTime().equals(this.getLastAccessEndTime());
	}

	/**
	 * Returns the time this session was created.
	 * @return the time this session was created
	 */
	Instant getCreationTime();

	/**
	 * Returns the start time of the last request to access this session, or null if session was created during the current request.
	 * @return the start time of the last request to access this session, or null if session was created during the current request.
	 */
	Instant getLastAccessStartTime();

	/**
	 * Returns the end time of the last request to access this session, or null if session was created during the current request.
	 * @return the end time of the last request to access this session, or null if session was created during the current request.
	 */
	Instant getLastAccessEndTime();

	@Override
	default Instant getLastAccessTime() {
		return this.getLastAccessEndTime();
	}
}
