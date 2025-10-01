/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import java.time.Duration;

import org.wildfly.clustering.server.offset.OffsetValue;

/**
 * A set of session access metadata values.
 * @author Paul Ferraro
 */
public interface MutableSessionAccessMetaDataOffsetValues extends MutableSessionAccessMetaDataValues {

	/**
	 * Creates mutable session access metadata offset values.
	 * @param accessMetaData the session access metadata
	 * @return mutable session access metadata offset values.
	 */
	static MutableSessionAccessMetaDataOffsetValues from(ImmutableSessionAccessMetaData accessMetaData) {
		OffsetValue<Duration> sinceCreation = OffsetValue.from(accessMetaData.getSinceCreationDuration());
		OffsetValue<Duration> lastAccess = OffsetValue.from(accessMetaData.getLastAccessDuration());
		return new MutableSessionAccessMetaDataOffsetValues() {
			@Override
			public OffsetValue<Duration> getSinceCreation() {
				return sinceCreation;
			}

			@Override
			public OffsetValue<Duration> getLastAccess() {
				return lastAccess;
			}
		};
	}

	@Override
	OffsetValue<Duration> getSinceCreation();

	@Override
	OffsetValue<Duration> getLastAccess();
}
