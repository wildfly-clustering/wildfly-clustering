/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.wildfly.clustering.session.cache.metadata.InvalidatableSessionMetaData;

/**
 * Composite view of the meta data of a session, combining volatile and static aspects.
 * @author Paul Ferraro
 */
public class CompositeSessionMetaData extends CompositeImmutableSessionMetaData implements InvalidatableSessionMetaData {

	private final SessionCreationMetaData creationMetaData;
	private final SessionAccessMetaData accessMetaData;
	private final Runnable mutator;
	private final AtomicBoolean valid = new AtomicBoolean(true);

	public CompositeSessionMetaData(SessionCreationMetaData creationMetaData, SessionAccessMetaData accessMetaData, Runnable mutator) {
		super(creationMetaData, accessMetaData);
		this.creationMetaData = creationMetaData;
		this.accessMetaData = accessMetaData;
		this.mutator = mutator;
	}

	@Override
	public boolean isValid() {
		return this.valid.get();
	}

	@Override
	public boolean invalidate() {
		return this.valid.compareAndSet(true, false);
	}

	@Override
	public void setLastAccess(Instant startTime, Instant endTime) {
		if (startTime.isAfter(endTime)) {
			throw new IllegalStateException();
		}
		Instant creationTime = this.creationMetaData.getCreationTime();
		// Retain millisecond precision
		Instant normalizedStartTime = startTime.truncatedTo(ChronoUnit.MILLIS);
		// Retain second precision for last access duration
		Duration lastAccess = Duration.between(startTime, endTime.truncatedTo(ChronoUnit.MILLIS));
		long seconds = lastAccess.getSeconds();
		// Round up
		if (lastAccess.getNano() > 0) {
			seconds += 1;
		}
		Duration normalizedLastAccess = (seconds > 1) ? Duration.ofSeconds(seconds) : ChronoUnit.SECONDS.getDuration();
		this.accessMetaData.setLastAccessDuration(Duration.between(creationTime, normalizedStartTime), normalizedLastAccess);
	}

	@Override
	public void setTimeout(Duration duration) {
		this.creationMetaData.setTimeout(duration.isNegative() ? Duration.ZERO : duration);
	}

	@Override
	public void close() {
		this.mutator.run();
	}
}
