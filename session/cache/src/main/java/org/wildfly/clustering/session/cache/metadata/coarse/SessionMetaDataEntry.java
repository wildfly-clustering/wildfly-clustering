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
	 * Sets the session timeout.
	 * @param the session timeout
	 */
	void setTimeout(Duration timeout);
}
