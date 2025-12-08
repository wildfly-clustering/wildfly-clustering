/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import java.time.Duration;
import java.time.Instant;

import org.wildfly.clustering.cache.function.RemappingFunction;
import org.wildfly.clustering.server.offset.Offset;

/**
 * Cache compute function that applies the session meta data delta.
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public class SessionMetaDataEntryFunction<C> extends RemappingFunction<ContextualSessionMetaDataEntry<C>, SessionMetaDataEntryOffsets> {
	/**
	 * Creates a session metadata entry function.
	 * @param values the set of offset values
	 */
	public SessionMetaDataEntryFunction(MutableSessionMetaDataOffsetValues values) {
		this(new SessionMetaDataEntryOffsets() {
			@Override
			public Offset<Duration> getMaxIdleOffset() {
				return values.getMaxIdle().getOffset();
			}

			@Override
			public Offset<Instant> getLastAccessStartTimeOffset() {
				return values.getLastAccessStartTime().getOffset();
			}

			@Override
			public Offset<Instant> getLastAccessEndTimeOffset() {
				return values.getLastAccessEndTime().getOffset();
			}
		});
	}

	/**
	 * Creates a session metadata entry function from the specified operand
	 * @param operand the remapping function operand
	 */
	public SessionMetaDataEntryFunction(SessionMetaDataEntryOffsets operand) {
		super(operand);
	}
}
