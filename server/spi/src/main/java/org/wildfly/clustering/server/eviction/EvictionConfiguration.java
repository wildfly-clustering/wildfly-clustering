/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.eviction;

import java.time.Duration;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Encapsulates eviction configuration.
 * @author Paul Ferraro
 */
public interface EvictionConfiguration {

	/**
	 * When present, defines the maximum number of elements to retain in memory.
	 * @return an option size-based eviction threshold.
	 */
	default OptionalInt getMaxSize() {
		return OptionalInt.empty();
	}

	/**
	 * When present, defines the maximum duration of time that an idle entry should remain in memory.
	 * @return an option time-based eviction threshold.
	 */
	default Optional<Duration> getIdleTimeout() {
		return Optional.empty();
	}
}
