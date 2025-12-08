/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import java.time.Duration;
import java.time.Instant;

import org.wildfly.clustering.server.offset.Value;

/**
 * Encapsulates the mutable values of the session metadata.
 * @author Paul Ferraro
 */
public interface MutableSessionMetaDataValues {
	/**
	 * Returns the max idle duration value
	 * @return the max idle duration value.
	 */
	Value<Duration> getMaxIdle();

	/**
	 * Returns the last access start time value.
	 * @return the last access start time value.
	 */
	Value<Instant> getLastAccessStartTime();

	/**
	 * Returns the last access end time value.
	 * @return the last access end time value.
	 */
	Value<Instant> getLastAccessEndTime();
}
