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

	Offset<Duration> getSinceCreationOffset();

	Offset<Duration> getLastAccessOffset();
}
