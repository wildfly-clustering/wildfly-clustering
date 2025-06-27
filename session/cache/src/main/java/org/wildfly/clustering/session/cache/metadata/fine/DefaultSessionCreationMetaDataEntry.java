/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.server.offset.Offset;
import org.wildfly.clustering.server.util.Supplied;

/**
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public class DefaultSessionCreationMetaDataEntry<C> implements SessionCreationMetaDataEntry<C> {

	private final Instant creationTime;
	private volatile Duration timeout = Duration.ZERO;
	private final Supplied<C> context = Supplied.cached();

	public DefaultSessionCreationMetaDataEntry() {
		// Only retain millisecond precision
		this(Instant.now().truncatedTo(ChronoUnit.MILLIS));
	}

	public DefaultSessionCreationMetaDataEntry(Instant creationTime) {
		this.creationTime = creationTime;
	}

	@Override
	public Instant getCreationTime() {
		return this.creationTime;
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
	public Supplied<C> getContext() {
		return this.context;
	}

	@Override
	public SessionCreationMetaDataEntry<C> remap(java.util.function.Supplier<Offset<Duration>> timeoutOffset) {
		SessionCreationMetaDataEntry<C> result = new DefaultSessionCreationMetaDataEntry<>(this.creationTime);
		result.setTimeout(timeoutOffset.get().apply(this.timeout));
		result.getContext().get(Supplier.of(this.context.get(Supplier.empty())));
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(this.getClass().getSimpleName()).append(" { ");
		builder.append("created = ").append(this.creationTime);
		builder.append(", timeout = ").append(this.timeout);
		return builder.append(" }").toString();
	}
}
