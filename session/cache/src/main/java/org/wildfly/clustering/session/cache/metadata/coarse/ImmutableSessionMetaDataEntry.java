/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

/**
 * An immutable view of the session metadata entry.
 * @author Paul Ferraro
 */
public interface ImmutableSessionMetaDataEntry {

	/**
	 * Returns true, if this is a newly created entry, false otherwise.
	 * @return true, if this is a newly created entry, false otherwise.
	 */
	boolean isNew();

	/**
	 * Returns the time this entry was created.
	 * @return the creation time
	 */
	Instant getCreationTime();

	/**
	 * Returns the last access start time, as an offset of the creation time.
	 * @return the last access start time, as an offset of the creation time.
	 */
	Supplier<Instant> getLastAccessStartTime();

	/**
	 * Returns the last access end time, as an offset of the last access start time.
	 * @return the last access end time, as an offset of the last access start time.
	 */
	Supplier<Instant> getLastAccessEndTime();

	/**
	 * Returns the duration of time since last access that this session should expire.
	 * An immortal session will return {@link Duration#ZERO}.
	 * @return the duration of time since last access that this session should expire.
	 */
	Duration getMaxIdle();
}
