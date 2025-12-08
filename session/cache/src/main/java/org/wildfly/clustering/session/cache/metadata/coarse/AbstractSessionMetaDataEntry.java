/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import java.util.Map;

/**
 * A session meta data entry.
 * @author Paul Ferraro
 */
public abstract class AbstractSessionMetaDataEntry implements SessionMetaDataEntry {

	AbstractSessionMetaDataEntry() {
	}

	@Override
	public String toString() {
		return Map.of(
				"creation-time", this.getCreationTime(),
				"last-access-start", this.getLastAccessStartTime().get(),
				"last-access-end", this.getLastAccessEndTime().get(),
				"max-idle", this.getMaxIdle())
				.toString();
	}
}
