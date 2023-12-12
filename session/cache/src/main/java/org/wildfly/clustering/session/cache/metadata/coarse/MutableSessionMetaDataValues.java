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
	Value<Duration> getTimeout();

	Value<Instant> getLastAccessStartTime();

	Value<Instant> getLastAccessEndTime();
}
