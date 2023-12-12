/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;

import java.time.Duration;
import java.time.Instant;

import org.mockito.Mockito;
import org.wildfly.clustering.server.offset.OffsetValue;
import org.wildfly.clustering.session.ImmutableSessionMetaData;

/**
 * @author Paul Ferraro
 */
public abstract class AbstractImmutableSessionMetaDataTestCase {

	void testCreationTime(ImmutableSessionMetaDataEntry entry, ImmutableSessionMetaData metaData) {
		Instant expected = Instant.now();

		doReturn(expected).when(entry).getCreationTime();

		Instant result = metaData.getCreationTime();

		assertSame(expected, result);
	}

	void testLastAccessStartTime(ImmutableSessionMetaDataEntry entry, ImmutableSessionMetaData metaData) {
		Instant expected = Instant.now();
		OffsetValue<Instant> lastAccessStartTime = Mockito.mock(OffsetValue.class);

		doReturn(lastAccessStartTime).when(entry).getLastAccessStartTime();
		doReturn(expected).when(lastAccessStartTime).get();

		Instant result = metaData.getLastAccessStartTime();

		assertSame(expected, result);
	}

	void testLastAccessEndTime(ImmutableSessionMetaDataEntry entry, ImmutableSessionMetaData metaData) {
		Instant expected = Instant.now();
		OffsetValue<Instant> lastAccessEndTime = Mockito.mock(OffsetValue.class);

		doReturn(lastAccessEndTime).when(entry).getLastAccessEndTime();
		doReturn(expected).when(lastAccessEndTime).get();

		Instant result = metaData.getLastAccessEndTime();

		assertEquals(expected, result);
		assertEquals(expected, metaData.getLastAccessTime());
	}

	void testTimeout(ImmutableSessionMetaDataEntry entry, ImmutableSessionMetaData metaData) {
		Duration expected = Duration.ofMinutes(60);

		doReturn(expected).when(entry).getTimeout();

		Duration result = metaData.getTimeout();

		assertSame(expected, result);
	}
}
