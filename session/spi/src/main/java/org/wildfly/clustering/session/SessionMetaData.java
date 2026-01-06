/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Abstraction for meta information about a session.
 * @author Paul Ferraro
 */
public interface SessionMetaData extends ImmutableSessionMetaData {
	/**
	 * Sets the time this session was last accessed.
	 * @param startTime the start time of the last request
	 * @param endTime the end time of the last request
	 */
	void setLastAccess(Instant startTime, Instant endTime);

	/**
	 * Sets the time this session was last accessed.
	 * @param entry an entry containing the start and end time of the last request
	 */
	default void setLastAccess(Map.Entry<Instant, Instant> entry) {
		this.setLastAccess(entry.getKey(), entry.getValue());
	}

	/**
	 * Specifies the duration of time since last access after which this session will expire.
	 * @param maxIdle the maximum idle duration
	 */
	void setMaxIdle(Duration maxIdle);
}
