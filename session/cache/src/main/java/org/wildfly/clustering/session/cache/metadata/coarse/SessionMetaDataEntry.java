/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import java.time.Duration;
import java.time.Instant;

import org.wildfly.clustering.server.offset.Value;

/**
 * Encapsulates the immutable cache entry properties storing session metadata.
 * @author Paul Ferraro
 */
public interface SessionMetaDataEntry extends ImmutableSessionMetaDataEntry {
	@Override
	Value<Instant> getLastAccessStartTime();

	@Override
	Value<Instant> getLastAccessEndTime();

	/**
	 * Specifies the duration of time since last access after which this session will expire.
	 * @param maxIdle the duration of time since last access after which this session will expire.
	 */
	void setMaxIdle(Duration maxIdle);
}
