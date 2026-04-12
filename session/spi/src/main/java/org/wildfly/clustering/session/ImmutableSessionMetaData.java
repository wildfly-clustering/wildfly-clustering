/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Predicate;
import org.wildfly.clustering.server.expiration.ExpirationMetaData;

/**
 * Abstraction for immutable meta information about a session.
 * @author Paul Ferraro
 */
public interface ImmutableSessionMetaData extends ExpirationMetaData {
	/** A function returning the creation time for a session */
	Function<ImmutableSessionMetaData, Instant> CREATION_TIME = ImmutableSessionMetaData::getCreationTime;
	/** A function returning the start time of the last access of a session, of empty */
	Function<ImmutableSessionMetaData, Optional<Instant>> LAST_ACCESS_START_TIME = ImmutableSessionMetaData::getLastAccessStartTime;
	/** A function returning the start time of the last access of a session, or the creation time for new sessions */
	Function<ImmutableSessionMetaData, Instant> LAST_ACCESS_TIME = Function.when(Predicate.of(LAST_ACCESS_START_TIME, Optional::isPresent), Function.of(LAST_ACCESS_START_TIME, Optional::get), CREATION_TIME);
	/** A function returning the maximum idle duration for a session */
	Function<ImmutableSessionMetaData, Optional<Duration>> MAX_IDLE = ImmutableSessionMetaData::getMaxIdle;

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

	/**
	 * If present, returns and entry containing the start and end time of the last request to access this session.
	 * @return an entry containing the start and end time of the last request to access this session, or empty if session was created during the current request.
	 */
	default Optional<Map.Entry<Instant, Instant>> getLastAccess() {
		Instant startTime = this.getLastAccessStartTime().orElse(null);
		Instant endTime = this.getLastAccessEndTime().orElse(null);
		return (startTime != null) && (endTime != null) ? Optional.of(Map.entry(startTime, endTime)) : Optional.empty();
	}
}
