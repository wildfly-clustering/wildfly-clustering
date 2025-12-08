/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Predicate;

import org.wildfly.clustering.session.cache.metadata.AbstractImmutableSessionMetaData;

/**
 * Default immutable session metadata implementation that delegates to a cache entry.
 * @author Paul Ferraro
 */
public class DefaultImmutableSessionMetaData extends AbstractImmutableSessionMetaData {
	private static final Predicate<Duration> MORTAL = Predicate.not(Duration::isZero);

	private final ImmutableSessionMetaDataEntry entry;

	/**
	 * Immutable session metadata referencing the specified entry.
	 * @param entry a session metadata entry
	 */
	public DefaultImmutableSessionMetaData(ImmutableSessionMetaDataEntry entry) {
		this.entry = entry;
	}

	@Override
	public Instant getCreationTime() {
		return this.entry.getCreationTime();
	}

	@Override
	public Optional<Instant> getLastAccessStartTime() {
		return !this.entry.isNew() ? Optional.of(this.entry.getLastAccessStartTime().get()) : Optional.empty();
	}

	@Override
	public Optional<Instant> getLastAccessEndTime() {
		return !this.entry.isNew() ? Optional.of(this.entry.getLastAccessEndTime().get()) : Optional.empty();
	}

	@Override
	public Optional<Duration> getMaxIdle() {
		return Optional.of(this.entry.getMaxIdle()).filter(MORTAL);
	}
}
