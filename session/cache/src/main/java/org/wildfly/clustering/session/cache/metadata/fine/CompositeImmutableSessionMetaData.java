/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import java.time.Duration;
import java.time.Instant;

import org.wildfly.clustering.session.ImmutableSessionMetaData;

/**
 * Immutable session metadata composed of separate creation and access meta data.
 * @author Paul Ferraro
 */
public class CompositeImmutableSessionMetaData implements ImmutableSessionMetaData {

	private final ImmutableSessionCreationMetaData creationMetaData;
	private final ImmutableSessionAccessMetaData accessMetaData;

	/**
	 * Creates composite immutable session metadata.
	 * @param creationMetaData the creation metadata
	 * @param accessMetaData the access metadata
	 */
	public CompositeImmutableSessionMetaData(ImmutableSessionCreationMetaData creationMetaData, ImmutableSessionAccessMetaData accessMetaData) {
		this.creationMetaData = creationMetaData;
		this.accessMetaData = accessMetaData;
	}

	@Override
	public boolean isNew() {
		return this.accessMetaData.isNew();
	}

	@Override
	public Duration getTimeout() {
		return this.creationMetaData.getTimeout();
	}

	@Override
	public Instant getCreationTime() {
		return this.creationMetaData.getCreationTime();
	}

	@Override
	public Instant getLastAccessStartTime() {
		return !this.isNew() ? this.getCreationTime().plus(this.accessMetaData.getSinceCreationDuration()) : null;
	}

	@Override
	public Instant getLastAccessEndTime() {
		return !this.isNew() ? this.getLastAccessStartTime().plus(this.accessMetaData.getLastAccessDuration()) : null;
	}

	@Override
	public String toString() {
		return String.format("{ new = %s, creation-time = %s, last-access-start-time = %s, last-access-end-time = %s, timeout = %s }", this.isNew(), this.getCreationTime(), this.getLastAccessStartTime(), this.getLastAccessEndTime(), this.getTimeout());
	}
}
