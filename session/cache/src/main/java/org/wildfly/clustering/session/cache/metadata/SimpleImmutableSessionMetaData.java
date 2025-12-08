/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache.metadata;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.wildfly.clustering.session.ImmutableSessionMetaData;

/**
 * An immutable "snapshot" of a session's meta-data which can be accessed outside the scope of a transaction.
 * @author Paul Ferraro
 */
public class SimpleImmutableSessionMetaData extends AbstractImmutableSessionMetaData {

	private final Instant creationTime;
	private final Optional<Instant> lastAccessStartTime;
	private final Optional<Instant> lastAccessEndTime;
	private final Optional<Duration> timeout;

	/**
	 * Creates an immutable snapshot from the specified metadata.
	 * @param metaData the metadata of a session
	 */
	public SimpleImmutableSessionMetaData(ImmutableSessionMetaData metaData) {
		this.creationTime = metaData.getCreationTime();
		this.lastAccessStartTime = metaData.getLastAccessStartTime();
		this.lastAccessEndTime = metaData.getLastAccessEndTime();
		this.timeout = metaData.getMaxIdle();
	}

	@Override
	public Instant getCreationTime() {
		return this.creationTime;
	}

	@Override
	public Optional<Instant> getLastAccessStartTime() {
		return this.lastAccessStartTime;
	}

	@Override
	public Optional<Instant> getLastAccessEndTime() {
		return this.lastAccessEndTime;
	}

	@Override
	public Optional<Duration> getMaxIdle() {
		return this.timeout;
	}
}
