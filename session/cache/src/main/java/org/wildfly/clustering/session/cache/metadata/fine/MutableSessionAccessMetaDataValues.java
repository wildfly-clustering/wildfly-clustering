/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import java.time.Duration;

import org.wildfly.clustering.server.offset.Value;

/**
 * @author Paul Ferraro
 */
public interface MutableSessionAccessMetaDataValues {
	Value<Duration> getSinceCreation();

	Value<Duration> getLastAccess();
}
