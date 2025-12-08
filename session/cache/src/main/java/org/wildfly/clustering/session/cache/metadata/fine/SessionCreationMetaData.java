/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import java.time.Duration;

/**
 * The more static aspects of a session's meta-data.
 * @author Paul Ferraro
 */
public interface SessionCreationMetaData extends ImmutableSessionCreationMetaData {
	/**
	 * Sets the maximum duration of time this session may remain idle before it will be expired by the session manager.
	 * @param maxIdle a maximum duration of time this session may remain idle before it will be expired by the session manager.
	 */
	void setMaxIdle(Duration maxIdle);
}
