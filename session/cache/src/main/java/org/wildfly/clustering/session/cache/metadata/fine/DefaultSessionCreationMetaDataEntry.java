/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.server.offset.Offset;
import org.wildfly.clustering.server.util.Supplied;

/**
 * The session creation metadata cache entry.
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public class DefaultSessionCreationMetaDataEntry<C> implements SessionCreationMetaDataEntry<C> {

	private final Instant creationTime;
	private volatile Duration timeout = Duration.ZERO;
	private final Supplied<C> context = Supplied.cached();

	/**
	 * Creates a session creation metadata entry for an existing session.
	 * @param creationTime the instant this session was created
	 */
	public DefaultSessionCreationMetaDataEntry(Instant creationTime) {
		this.creationTime = creationTime;
	}

	@Override
	public Instant getCreationTime() {
		return this.creationTime;
	}

	@Override
	public Duration getMaxIdle() {
		return this.timeout;
	}

	@Override
	public void setMaxIdle(Duration timeout) {
		this.timeout = timeout.isNegative() ? Duration.ZERO : timeout;
	}

	@Override
	public Supplied<C> getContext() {
		return this.context;
	}

	@Override
	public SessionCreationMetaDataEntry<C> remap(java.util.function.Supplier<Offset<Duration>> timeoutOffset) {
		SessionCreationMetaDataEntry<C> result = new DefaultSessionCreationMetaDataEntry<>(this.creationTime);
		result.setMaxIdle(timeoutOffset.get().apply(this.timeout));
		result.getContext().get(Supplier.of(this.context.get(Supplier.of(null))));
		return result;
	}

	@Override
	public String toString() {
		return Map.of("creation-time", this.creationTime, "timeout", this.timeout).toString();
	}
}
