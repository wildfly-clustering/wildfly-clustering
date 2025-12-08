/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.expiration;

import java.time.Duration;
import java.util.Optional;

/**
 * Describes the expiration criteria for managed state.
 * @author Paul Ferraro
 */
public interface Expiration {
	/**
	 * When present, defines the maximum duration of time since last access, after which managed state will expire.
	 * @return the optional duration of time since last access after which managed state will expire
	 */
	Optional<Duration> getMaxIdle();
}
