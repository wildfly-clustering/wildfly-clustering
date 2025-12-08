/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.offset.Offset;

/**
 * Abstract unit test for {@link SessionMetaDataEntry} implementations.
 * @author Paul Ferraro
 */
public abstract class AbstractSessionMetaDataEntryTestCase implements Consumer<ContextualSessionMetaDataEntry<Object>> {

	private final Instant originalLastAccessEndTime = Instant.now();
	private final Instant originalLastAccessStartTime = this.originalLastAccessEndTime.minus(Duration.ofSeconds(1));
	private final Instant created = this.originalLastAccessStartTime.minus(Duration.ofMinutes(1));
	private final Duration originalTimeout = Duration.ofMinutes(20);

	private final Duration lastAccessStartTimeDelta = Duration.ofSeconds(10);
	private final Duration lastAccessEndTimeDelta = Duration.ofSeconds(2);
	private final Duration timeoutDelta = Duration.ofMinutes(10);

	private final Instant updatedLastAccessStartTime = this.originalLastAccessStartTime.plus(this.lastAccessStartTimeDelta);
	private final Instant updatedLastAccessEndTime = this.originalLastAccessEndTime.plus(this.lastAccessEndTimeDelta);
	private final Duration updatedTimeout = this.originalTimeout.plus(this.timeoutDelta);

	@Test
	public void test() {
		DefaultSessionMetaDataEntry<Object> entry = new DefaultSessionMetaDataEntry<>(this.created);

		// Verify defaults
		assertThat(entry.getCreationTime()).isEqualTo(this.created);
		assertThat(entry.getLastAccessStartTime().get()).isEqualTo(this.created);
		assertThat(entry.getLastAccessEndTime().get()).isEqualTo(this.created);
		assertThat(entry.getMaxIdle()).isZero();
		assertThat(entry.getContext().get(() -> null)).isNull();

		// Apply original state
		entry.getLastAccessStartTime().set(this.originalLastAccessStartTime);
		entry.getLastAccessEndTime().set(this.originalLastAccessEndTime);
		entry.setMaxIdle(this.originalTimeout);

		this.verifyOriginalState(entry);

		// Verify remap
		SessionMetaDataEntryOffsets offsets = mock(SessionMetaDataEntryOffsets.class);
		doReturn(Offset.forDuration(this.timeoutDelta)).when(offsets).getMaxIdleOffset();
		doReturn(Offset.forInstant(this.lastAccessStartTimeDelta)).when(offsets).getLastAccessStartTimeOffset();
		doReturn(Offset.forInstant(this.lastAccessEndTimeDelta)).when(offsets).getLastAccessEndTimeOffset();

		ContextualSessionMetaDataEntry<Object> remapped = entry.remap(offsets);

		this.verifyUpdatedState(remapped);
		// Verify that remap is side-effect free
		this.verifyOriginalState(entry);

		// Implementation-specific validation
		this.accept(entry);
	}

	void updateState(SessionMetaDataEntry entry) {
		entry.getLastAccessStartTime().set(this.updatedLastAccessStartTime);
		entry.getLastAccessEndTime().set(this.updatedLastAccessEndTime);
		entry.setMaxIdle(this.updatedTimeout);
	}

	void verifyOriginalState(SessionMetaDataEntry entry) {
		assertThat(entry.getCreationTime()).isEqualTo(this.created);
		assertThat(entry.getLastAccessStartTime().get()).isEqualTo(this.originalLastAccessStartTime);
		assertThat(entry.getLastAccessEndTime().get()).isEqualTo(this.originalLastAccessEndTime);
		assertThat(entry.getMaxIdle()).isEqualTo(this.originalTimeout);
	}

	void verifyUpdatedState(SessionMetaDataEntry entry) {
		assertThat(entry.getCreationTime()).isEqualTo(this.created);
		assertThat(entry.getLastAccessStartTime().get()).isEqualTo(this.updatedLastAccessStartTime);
		assertThat(entry.getLastAccessEndTime().get()).isEqualTo(this.updatedLastAccessEndTime);
		assertThat(entry.getMaxIdle()).isEqualTo(this.updatedTimeout);
	}
}
