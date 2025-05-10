/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.expiration;

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
		return expirationTime.isPresent() && !expirationTime.get().isAfter(Instant.now());
	}

	/**
	 * Returns the time at which this session should expire.
	 * @return an optional expiration time, present if the session is mortal and not new.
	 */
	default Optional<Instant> getExpirationTime() {
		if (this.isImmortal()) return Optional.empty();
		Instant lastAccessedTime = this.getLastAccessTime();
		return (lastAccessedTime != null) ? Optional.of(lastAccessedTime.plus(this.getTimeout())) : Optional.empty();
	}

	/**
	 * Returns the time this object was last accessed.
	 * @return the time this object was last accessed.
	 */
	Instant getLastAccessTime();
}
