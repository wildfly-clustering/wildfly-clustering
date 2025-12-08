/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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

		assertThat(result).isSameAs(expected);
	}

	void testLastAccessStartTime(ImmutableSessionMetaDataEntry entry, ImmutableSessionMetaData metaData) {
		Instant expected = Instant.now();
		OffsetValue<Instant> lastAccessStartTime = Mockito.mock(OffsetValue.class);

		doReturn(false).when(entry).isNew();
		doReturn(lastAccessStartTime).when(entry).getLastAccessStartTime();
		doReturn(expected).when(lastAccessStartTime).get();

		assertThat(metaData.getLastAccessStartTime()).hasValue(expected);

		doReturn(true).when(entry).isNew();

		assertThat(metaData.getLastAccessStartTime()).isEmpty();
	}

	void testLastAccessEndTime(ImmutableSessionMetaDataEntry entry, ImmutableSessionMetaData metaData) {
		Instant expected = Instant.now();
		OffsetValue<Instant> lastAccessEndTime = Mockito.mock(OffsetValue.class);

		doReturn(false).when(entry).isNew();
		doReturn(lastAccessEndTime).when(entry).getLastAccessEndTime();
		doReturn(expected).when(lastAccessEndTime).get();

		assertThat(metaData.getLastAccessEndTime()).hasValue(expected);
		assertThat(metaData.getLastAccessTime()).hasValue(expected);

		doReturn(true).when(entry).isNew();

		assertThat(metaData.getLastAccessEndTime()).isEmpty();
	}

	void testTimeout(ImmutableSessionMetaDataEntry entry, ImmutableSessionMetaData metaData) {
		Duration expected = Duration.ofMinutes(60);

		doReturn(Duration.ZERO, expected).when(entry).getMaxIdle();

		assertThat(metaData.getMaxIdle()).isEmpty();
		assertThat(metaData.getMaxIdle()).hasValue(expected);
	}
}
