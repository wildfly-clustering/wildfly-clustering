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

	void isNew(ImmutableSessionCreationMetaData creationMetaData, ImmutableSessionAccessMetaData accessMetaData, ImmutableSessionMetaData metaData) {
		when(accessMetaData.isNew()).thenReturn(true);

		assertThat(metaData.isNew()).isTrue();

		when(accessMetaData.isNew()).thenReturn(false);

		assertThat(metaData.isNew()).isFalse();
	}

	void isExpired(ImmutableSessionCreationMetaData creationMetaData, ImmutableSessionAccessMetaData accessMetaData, ImmutableSessionMetaData metaData) {
		when(creationMetaData.getCreationTime()).thenReturn(Instant.now().minus(Duration.ofMinutes(10L)));
		when(creationMetaData.getTimeout()).thenReturn(Duration.ofMinutes(10L));
		when(accessMetaData.getSinceCreationDuration()).thenReturn(Duration.ofMinutes(5L));
		when(accessMetaData.getLastAccessDuration()).thenReturn(Duration.ofSeconds(1));

		assertThat(metaData.isExpired()).isFalse();

		when(creationMetaData.getTimeout()).thenReturn(Duration.ofMinutes(5L).minus(Duration.ofSeconds(1, 1)));

		assertThat(metaData.isExpired()).isTrue();

		// Timeout of 0 means never expire
		when(creationMetaData.getTimeout()).thenReturn(Duration.ZERO);

		assertThat(metaData.isExpired()).isFalse();
	}

	void getCreationTime(ImmutableSessionCreationMetaData creationMetaData, ImmutableSessionAccessMetaData accessMetaData, ImmutableSessionMetaData metaData) {
		Instant expected = Instant.now();

		when(creationMetaData.getCreationTime()).thenReturn(expected);

		Instant result = metaData.getCreationTime();

		assertThat(result).isSameAs(expected);
	}

	void getLastAccessStartTime(ImmutableSessionCreationMetaData creationMetaData, ImmutableSessionAccessMetaData accessMetaData, ImmutableSessionMetaData metaData) {
		Instant now = Instant.now();
		Duration sinceCreation = Duration.ofSeconds(10L);

		when(creationMetaData.getCreationTime()).thenReturn(now.minus(sinceCreation));
		when(accessMetaData.getSinceCreationDuration()).thenReturn(sinceCreation);

		Instant result = metaData.getLastAccessStartTime();

		assertThat(result).isEqualTo(now);
	}

	void getLastAccessEndTime(ImmutableSessionCreationMetaData creationMetaData, ImmutableSessionAccessMetaData accessMetaData, ImmutableSessionMetaData metaData) {
		Instant now = Instant.now();
		Duration sinceCreation = Duration.ofSeconds(10L);
		Duration lastAccess = Duration.ofSeconds(1L);

		when(creationMetaData.getCreationTime()).thenReturn(now.minus(sinceCreation).minus(lastAccess));
		when(accessMetaData.getSinceCreationDuration()).thenReturn(sinceCreation);
		when(accessMetaData.getLastAccessDuration()).thenReturn(lastAccess);

		Instant result = metaData.getLastAccessEndTime();

		assertThat(result).isEqualTo(now);
	}

	void getMaxInactiveInterval(ImmutableSessionCreationMetaData creationMetaData, ImmutableSessionAccessMetaData accessMetaData, ImmutableSessionMetaData metaData) {
		Duration expected = Duration.ofMinutes(30L);

		when(creationMetaData.getTimeout()).thenReturn(expected);

		Duration result = metaData.getTimeout();

		assertThat(result).isSameAs(expected);
	}
}
