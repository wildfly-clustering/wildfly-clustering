/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import java.time.Duration;
import java.time.Instant;

import org.wildfly.clustering.server.offset.OffsetValue;

/**
 * Encapsulates the mutable session metadata entry properties, captured as offsets from their current values.
 * @author Paul Ferraro
 */
public interface MutableSessionMetaDataOffsetValues extends MutableSessionMetaDataValues {

	/**
	 * Creates a mutable session metadata entry delta from the specified metadata entry.
	 * @param <C> the session context type
	 * @param entry a session metadata entry
	 * @return an object encapsulating the mutable session meta data properties
	 */
	static <C> MutableSessionMetaDataOffsetValues from(ContextualSessionMetaDataEntry<C> entry) {
		OffsetValue<Duration> maxIdle = OffsetValue.from(entry.getMaxIdle());
		OffsetValue<Instant> lastAccessStartTime = entry.getLastAccessStartTime().rebase();
		OffsetValue<Instant> lastAccessEndTime = entry.getLastAccessEndTime().rebase();
		return new MutableSessionMetaDataOffsetValues() {
			@Override
			public OffsetValue<Duration> getMaxIdle() {
				return maxIdle;
			}

			@Override
			public OffsetValue<Instant> getLastAccessStartTime() {
				return lastAccessStartTime;
			}

			@Override
			public OffsetValue<Instant> getLastAccessEndTime() {
				return lastAccessEndTime;
			}
		};
	}

	@Override
	OffsetValue<Duration> getMaxIdle();

	@Override
	OffsetValue<Instant> getLastAccessStartTime();

	@Override
	OffsetValue<Instant> getLastAccessEndTime();
}
