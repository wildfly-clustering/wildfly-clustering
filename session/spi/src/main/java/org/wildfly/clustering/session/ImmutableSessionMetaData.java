/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session;

import java.time.Instant;
import java.util.Optional;

import org.wildfly.clustering.server.expiration.ExpirationMetaData;

/**
 * Abstraction for immutable meta information about a session.
 * @author Paul Ferraro
 */
public interface ImmutableSessionMetaData extends ExpirationMetaData {

	/**
	 * Returns the time this session was created.
	 * @return the time this session was created
	 */
	Instant getCreationTime();

	/**
	 * If present, returns the start time of the last request to access this session.
	 * @return the start time of the last request to access this session, or empty if session was created during the current request.
	 */
	Optional<Instant> getLastAccessStartTime();

	/**
	 * If present, returns the end time of the last request to access this session.
	 * @return the end time of the last request to access this session, or empty if session was created during the current request.
	 */
	Optional<Instant> getLastAccessEndTime();

	@Override
	default Optional<Instant> getLastAccessTime() {
		return this.getLastAccessEndTime();
	}
}
