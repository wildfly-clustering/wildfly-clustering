/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import java.time.Duration;
import java.time.Instant;

import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.server.offset.OffsetValue;
import org.wildfly.clustering.server.util.Supplied;

/**
 * Default contextual session metadata entry.
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public class DefaultSessionMetaDataEntry<C> extends AbstractSessionMetaDataEntry implements ContextualSessionMetaDataEntry<C> {

	private volatile Duration timeout = Duration.ZERO;
	// The start time of the last access, expressed as an offset from the creation time
	private final OffsetValue<Instant> lastAccessStartTime;
	// The end time of the last access, expressed an an offset from the start time of the last access
	private final OffsetValue<Instant> lastAccessEndTime;
	private final Supplied<C> context = Supplied.cached();

	/**
	 * Create a session metadata entry for an existing session.
	 * @param creationTime the instant this session was created.
	 */
	public DefaultSessionMetaDataEntry(Instant creationTime) {
		this.lastAccessStartTime = OffsetValue.from(creationTime);
		this.lastAccessEndTime = this.lastAccessStartTime.rebase();
	}

	@Override
	public boolean isNew() {
		return this.getLastAccessEndTime().getOffset().isZero();
	}

	@Override
	public Duration getTimeout() {
		return this.timeout;
	}

	@Override
	public void setTimeout(Duration timeout) {
		this.timeout = timeout;
	}

	@Override
	public OffsetValue<Instant> getLastAccessStartTime() {
		return this.lastAccessStartTime;
	}

	@Override
	public OffsetValue<Instant> getLastAccessEndTime() {
		return this.lastAccessEndTime;
	}

	@Override
	public Supplied<C> getContext() {
		return this.context;
	}

	@Override
	public ContextualSessionMetaDataEntry<C> remap(SessionMetaDataEntryOffsets offsets) {
		ContextualSessionMetaDataEntry<C> result = new DefaultSessionMetaDataEntry<>(this.getCreationTime());
		result.setTimeout(offsets.getTimeoutOffset().apply(this.timeout));
		result.getLastAccessStartTime().set(offsets.getLastAccessStartTimeOffset().apply(this.lastAccessStartTime.get()));
		result.getLastAccessEndTime().set(offsets.getLastAccessEndTimeOffset().apply(this.lastAccessEndTime.get()));
		result.getContext().get(Supplier.of(this.context.get(Supplier.empty())));
		return result;
	}
}
