/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache.metadata.fine;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;

import org.wildfly.clustering.session.ImmutableSessionMetaData;

/**
 * Unit test for {@link CompositeImmutableSessionMetaData}.
 * @author Paul Ferraro
 */
public abstract class AbstractImmutableSessionMetaDataTestCase {

	void isExpired(ImmutableSessionCreationMetaData creationMetaData, ImmutableSessionAccessMetaData accessMetaData, ImmutableSessionMetaData metaData) {
		doReturn(Instant.now().minus(Duration.ofMinutes(10L))).when(creationMetaData).getCreationTime();
		doReturn(Duration.ofMinutes(10L), Duration.ofMinutes(5L).minus(Duration.ofSeconds(1, 1)), Duration.ZERO).when(creationMetaData).getMaxIdle();
		doReturn(Duration.ofMinutes(5L)).when(accessMetaData).getSinceCreationDuration();
		doReturn(Duration.ofSeconds(1)).when(accessMetaData).getLastAccessDuration();

		assertThat(metaData.isExpired()).isFalse();
		assertThat(metaData.isExpired()).isTrue();
		// Timeout of 0 means never expire
		assertThat(metaData.isExpired()).isFalse();
	}

	void getCreationTime(ImmutableSessionCreationMetaData creationMetaData, ImmutableSessionAccessMetaData accessMetaData, ImmutableSessionMetaData metaData) {
		Instant expected = Instant.now();

		when(creationMetaData.getCreationTime()).thenReturn(expected);

		Instant result = metaData.getCreationTime();

		assertThat(result).isSameAs(expected);
	}

	void getLastAccessStartTime(ImmutableSessionCreationMetaData creationMetaData, ImmutableSessionAccessMetaData accessMetaData, ImmutableSessionMetaData metaData) {
		doReturn(Duration.ZERO).when(accessMetaData).getLastAccessDuration();

		assertThat(metaData.getLastAccessStartTime()).isEmpty();

		Instant now = Instant.now();
		Duration sinceCreation = Duration.ofSeconds(10L);

		doReturn(Duration.ofSeconds(1)).when(accessMetaData).getLastAccessDuration();
		doReturn(now.minus(sinceCreation)).when(creationMetaData).getCreationTime();
		doReturn(sinceCreation).when(accessMetaData).getSinceCreationDuration();

		assertThat(metaData.getLastAccessStartTime()).hasValue(now);
	}

	void getLastAccessEndTime(ImmutableSessionCreationMetaData creationMetaData, ImmutableSessionAccessMetaData accessMetaData, ImmutableSessionMetaData metaData) {
		doReturn(Duration.ZERO).when(accessMetaData).getLastAccessDuration();

		assertThat(metaData.getLastAccessEndTime()).isEmpty();

		Instant now = Instant.now();
		Duration sinceCreation = Duration.ofSeconds(10L);
		Duration lastAccess = Duration.ofSeconds(1L);

		doReturn(Duration.ofSeconds(1)).when(accessMetaData).getLastAccessDuration();
		doReturn(now.minus(sinceCreation).minus(lastAccess)).when(creationMetaData).getCreationTime();
		doReturn(sinceCreation).when(accessMetaData).getSinceCreationDuration();
		doReturn(lastAccess).when(accessMetaData).getLastAccessDuration();

		assertThat(metaData.getLastAccessEndTime()).hasValue(now);
	}

	void getMaxIdle(ImmutableSessionCreationMetaData creationMetaData, ImmutableSessionAccessMetaData accessMetaData, ImmutableSessionMetaData metaData) {
		Duration expected = Duration.ofMinutes(30L);

		doReturn(Duration.ZERO, expected).when(creationMetaData).getMaxIdle();

		assertThat(metaData.getMaxIdle()).isEmpty();
		assertThat(metaData.getMaxIdle()).hasValue(expected);
	}
}
