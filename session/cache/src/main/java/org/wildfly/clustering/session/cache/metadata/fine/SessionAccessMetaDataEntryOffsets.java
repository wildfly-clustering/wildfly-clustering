/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import java.time.Duration;

import org.wildfly.clustering.server.offset.Offset;

/**
 * Encapsulates offsets for session access metadata.
 * @author Paul Ferraro
 */
public interface SessionAccessMetaDataEntryOffsets {
	/**
	 * Returns the offset since creation.
	 * @return the offset since creation.
	 */
	Offset<Duration> getSinceCreationOffset();

	/**
	 * Returns the last access offset.
	 * @return the last access offset.
	 */
	Offset<Duration> getLastAccessOffset();
}
