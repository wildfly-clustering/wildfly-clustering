/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.expiration;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.wildfly.clustering.server.expiration.ExpirationMetaData;

/**
 * A cached {@link ExpirationMetaData} implementation.
 * @author Paul Ferraro
 */
public class SimpleExpirationMetaData implements ExpirationMetaData {

	private final Optional<Duration> maxIdle;
	private final Optional<Instant> lastAccessTime;

	/**
	 * Creates a cached expiration meta data.
	 * @param metaData expiration metadata
	 */
	public SimpleExpirationMetaData(ExpirationMetaData metaData) {
		this(metaData.getMaxIdle(), metaData.getLastAccessTime());
	}

	SimpleExpirationMetaData(Optional<Duration> maxIdle, Optional<Instant> lastAccessedTime) {
		this.maxIdle = maxIdle;
		this.lastAccessTime = lastAccessedTime;
	}

	@Override
	public Optional<Duration> getMaxIdle() {
		return this.maxIdle;
	}

	@Override
	public Optional<Instant> getLastAccessTime() {
		return this.lastAccessTime;
	}

	@Override
	public String toString() {
		return Map.of("max-idle", this.maxIdle, "last-access-time", this.lastAccessTime).toString();
	}
}
