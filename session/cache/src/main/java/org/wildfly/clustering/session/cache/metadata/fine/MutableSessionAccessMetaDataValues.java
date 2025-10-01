/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import java.time.Duration;

import org.wildfly.clustering.server.offset.Value;

/**
 * The mutable session access metadata values.
 * @author Paul Ferraro
 */
public interface MutableSessionAccessMetaDataValues {
	/**
	 * Returns the duration since creation value.
	 * @return the duration since creation value.
	 */
	Value<Duration> getSinceCreation();

	/**
	 * Returns the duration since last access.
	 * @return the duration since last access.
	 */
	Value<Duration> getLastAccess();
}
