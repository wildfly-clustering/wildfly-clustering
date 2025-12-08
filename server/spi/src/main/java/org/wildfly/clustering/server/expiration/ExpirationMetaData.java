/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.expiration;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Describes expiration-related metadata.
 * @author Paul Ferraro
 */
public interface ExpirationMetaData extends Expiration {

	/**
	 * Indicates whether or not this object is expired.
	 * @return true, if this object has expired, false otherwise.
	 */
	default boolean isExpired() {
		Optional<Instant> expirationTime = this.getExpirationTime();
		return expirationTime.isPresent() ? !Instant.now().isBefore(expirationTime.get()) : false;
	}

	/**
	 * Returns the time at which managed state will expire.
	 * @return an optional expiration time.
	 */
	default Optional<Instant> getExpirationTime() {
		Optional<Duration> maxIdle = this.getMaxIdle();
		return maxIdle.isPresent() ? this.getLastAccessTime().map(time -> time.plus(maxIdle.get())) : Optional.empty();
	}

	/**
	 * When present, returns the time this object was last accessed.
	 * @return the time this object was last accessed, or empty not previously accessed.
	 */
	Optional<Instant> getLastAccessTime();
}
