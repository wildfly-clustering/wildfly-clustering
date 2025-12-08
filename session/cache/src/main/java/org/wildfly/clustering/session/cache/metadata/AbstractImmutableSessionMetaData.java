/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata;

import java.util.Map;

import org.wildfly.clustering.session.ImmutableSessionMetaData;

/**
 * Common {@link #toString()} for session meta data.
 * @author Paul Ferraro
 */
public abstract class AbstractImmutableSessionMetaData implements ImmutableSessionMetaData {
	/**
	 * Creates an immutable session metadata.
	 */
	protected AbstractImmutableSessionMetaData() {
	}

	@Override
	public String toString() {
		return Map.of(
				"max-idle", this.getMaxIdle(),
				"creation-time", this.getCreationTime(),
				"last-access-start-time", this.getLastAccessStartTime(),
				"last-access-end-time", this.getLastAccessEndTime()
			).toString();
	}
}
