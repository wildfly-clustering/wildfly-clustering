/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import java.time.Duration;
import java.time.Instant;

/**
 * Immutable view of the more static aspects of a session's meta-data.
 * @author Paul Ferraro
 */
public interface ImmutableSessionCreationMetaData {
	/**
	 * Returns the time at which this session was created.
	 * @return the time at which this session was created
	 */
	Instant getCreationTime();

	/**
	 * Returns the maximum duration of time this session may remain idle before it will be expired by the session manager, or {@link Duration#ZERO} if the session will never expire.
	 * @return the maximum duration of time this session may remain idle before it will be expired by the session manager, or {@link Duration#ZERO} if the session will never expire.
	 */
	Duration getMaxIdle();
}
