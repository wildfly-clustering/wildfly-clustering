/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import java.time.Duration;
import java.time.Instant;

import org.wildfly.clustering.server.offset.Offset;

/**
 * Encapsulates session metadata entry offsets.
 * @author Paul Ferraro
 */
public interface SessionMetaDataEntryOffsets {
	/**
	 * Returns the max idle duration, as an offset from the current value.
	 * @return the max idle duration, as an offset from the current value.
	 */
	Offset<Duration> getMaxIdleOffset();

	/**
	 * Returns the last access start time, as an offset from the current value.
	 * @return the last access start time, as an offset from the current value.
	 */
	Offset<Instant> getLastAccessStartTimeOffset();

	/**
	 * Returns the last access end time, as an offset from the current value.
	 * @return the last access end time, as an offset from the current value.
	 */
	Offset<Instant> getLastAccessEndTimeOffset();
}
