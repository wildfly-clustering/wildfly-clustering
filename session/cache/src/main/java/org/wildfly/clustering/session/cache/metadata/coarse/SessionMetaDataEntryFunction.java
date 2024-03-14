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

	public SessionMetaDataEntryFunction(MutableSessionMetaDataOffsetValues delta) {
		this(new SessionMetaDataEntryOffsets() {
			@Override
			public Offset<Duration> getTimeoutOffset() {
				return delta.getTimeout().getOffset();
			}

			@Override
			public Offset<Instant> getLastAccessStartTimeOffset() {
				return delta.getLastAccessStartTime().getOffset();
			}

			@Override
			public Offset<Instant> getLastAccessEndTimeOffset() {
				return delta.getLastAccessEndTime().getOffset();
			}
		});
	}

	public SessionMetaDataEntryFunction(SessionMetaDataEntryOffsets operand) {
		super(operand);
	}
}
