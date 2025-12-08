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
	 * When present, defines the maximum number of elements to retain in memory, beyond which least recently used elements will be evicted.
	 * @return an optional eviction threshold size
	 */
	default OptionalInt getSizeThreshold() {
		return OptionalInt.empty();
	}

	/**
	 * When present, defines the duration of time after which managed state should be considered idle, and thus eligible for eviction.
	 * @return an optional eviction threshold duration
	 */
	default Optional<Duration> getIdleThreshold() {
		return Optional.empty();
	}
}
