/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Predicate;

import org.wildfly.clustering.session.cache.metadata.AbstractImmutableSessionMetaData;

/**
 * Immutable session metadata composed of separate creation and access meta data.
 * @author Paul Ferraro
 */
public class CompositeImmutableSessionMetaData extends AbstractImmutableSessionMetaData {
	private static final Predicate<Duration> MORTAL = Predicate.not(Duration::isZero);

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
	public Optional<Duration> getMaxIdle() {
		return Optional.of(this.creationMetaData.getMaxIdle()).filter(MORTAL);
	}

	@Override
	public Instant getCreationTime() {
		return this.creationMetaData.getCreationTime();
	}

	@Override
	public Optional<Instant> getLastAccessStartTime() {
		return !this.accessMetaData.getLastAccessDuration().isZero() ? Optional.of(this.getCreationTime().plus(this.accessMetaData.getSinceCreationDuration())) : Optional.empty();
	}

	@Override
	public Optional<Instant> getLastAccessEndTime() {
		return this.getLastAccessStartTime().map(start -> start.plus(this.accessMetaData.getLastAccessDuration()));
	}
}
