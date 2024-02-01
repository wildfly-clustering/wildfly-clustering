/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import java.time.Duration;
import java.util.Map;

/**
 * @author Paul Ferraro
 */
public class DefaultSessionAccessMetaDataEntry implements SessionAccessMetaDataEntry {

	private volatile Map.Entry<Duration, Duration> lastAccess = Map.entry(Duration.ZERO, Duration.ZERO);

	@Override
	public boolean isNew() {
		return this.getLastAccessDuration().isZero();
	}

	@Override
	public Duration getSinceCreationDuration() {
		return this.lastAccess.getKey();
	}

	@Override
	public Duration getLastAccessDuration() {
		return this.lastAccess.getValue();
	}

	@Override
	public void setLastAccessDuration(Duration sinceCreation, Duration lastAccess) {
		this.lastAccess = Map.entry(sinceCreation, lastAccess);
	}

	@Override
	public SessionAccessMetaDataEntry remap(SessionAccessMetaDataEntryOffsets offsets) {
		SessionAccessMetaDataEntry result = new DefaultSessionAccessMetaDataEntry();
		Map.Entry<Duration, Duration> lastAccess = this.lastAccess;
		result.setLastAccessDuration(offsets.getSinceCreationOffset().apply(lastAccess.getKey()), offsets.getLastAccessOffset().apply(lastAccess.getValue()));
		return result;
	}

	@Override
	public String toString() {
		Map.Entry<Duration, Duration> lastAccess = this.lastAccess;
		return String.format("{ since-creation = %s, last-access = %s }", lastAccess.getKey(), lastAccess.getValue());
	}
}
