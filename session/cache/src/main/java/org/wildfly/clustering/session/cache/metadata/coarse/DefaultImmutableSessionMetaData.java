/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import java.time.Duration;
import java.time.Instant;

import org.wildfly.clustering.session.ImmutableSessionMetaData;

/**
 * Default immutable session metadata implementation that delegates to a cache entry.
 * @author Paul Ferraro
 */
public class DefaultImmutableSessionMetaData implements ImmutableSessionMetaData {

	private final ImmutableSessionMetaDataEntry entry;

	/**
	 * Immutable session metadata referencing the specified entry.
	 * @param entry a session metadata entry
	 */
	public DefaultImmutableSessionMetaData(ImmutableSessionMetaDataEntry entry) {
		this.entry = entry;
	}

	@Override
	public boolean isNew() {
		return this.entry.isNew();
	}

	@Override
	public Instant getCreationTime() {
		return this.entry.getCreationTime();
	}

	@Override
	public Instant getLastAccessStartTime() {
		return !this.isNew() ? this.entry.getLastAccessStartTime().get() : null;
	}

	@Override
	public Instant getLastAccessEndTime() {
		return !this.isNew() ? this.entry.getLastAccessEndTime().get() : null;
	}

	@Override
	public Duration getTimeout() {
		return this.entry.getTimeout();
	}

	@Override
	public String toString() {
		return String.format("{ new = %s, creation-time = %s, last-access-start-time = %s, last-access-end-time = %s, timeout = %s }", this.isNew(), this.getCreationTime(), this.getLastAccessStartTime(), this.getLastAccessEndTime(), this.getTimeout());
	}
}
