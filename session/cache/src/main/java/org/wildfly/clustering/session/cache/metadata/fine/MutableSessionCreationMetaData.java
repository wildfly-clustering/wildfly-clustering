/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import java.time.Duration;
import java.time.Instant;

import org.wildfly.clustering.server.offset.Value;

/**
 * Mutable session creation metadata.
 * @author Paul Ferraro
 */
public class MutableSessionCreationMetaData implements SessionCreationMetaData {

	private final ImmutableSessionCreationMetaData metaData;
	private final Value<Duration> timeout;

	/**
	 * Creates mutable session creation metadata.
	 * @param metaData the immutable session creation metadata
	 * @param timeout the session timeout value.
	 */
	public MutableSessionCreationMetaData(ImmutableSessionCreationMetaData metaData, Value<Duration> timeout) {
		this.metaData = metaData;
		this.timeout = timeout;
	}

	@Override
	public Instant getCreationTime() {
		return this.metaData.getCreationTime();
	}

	@Override
	public Duration getTimeout() {
		return this.timeout.get();
	}

	@Override
	public void setTimeout(Duration timeout) {
		this.timeout.set(timeout);
	}
}
