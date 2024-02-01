/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import static org.junit.jupiter.api.Assertions.*;
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
		assertEquals(this.created, entry.getCreationTime());
		assertEquals(this.created, entry.getLastAccessStartTime().get());
		assertEquals(this.created, entry.getLastAccessEndTime().get());
		assertEquals(Duration.ZERO, entry.getTimeout());
		assertNull(entry.getContext().get(() -> null));

		// Apply original state
		entry.getLastAccessStartTime().set(this.originalLastAccessStartTime);
		entry.getLastAccessEndTime().set(this.originalLastAccessEndTime);
		entry.setTimeout(this.originalTimeout);

		this.verifyOriginalState(entry);

		// Verify remap
		SessionMetaDataEntryOffsets offsets = mock(SessionMetaDataEntryOffsets.class);
		doReturn(Offset.forDuration(this.timeoutDelta)).when(offsets).getTimeoutOffset();
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
		entry.setTimeout(this.updatedTimeout);
	}

	void verifyOriginalState(SessionMetaDataEntry entry) {
		assertEquals(this.created, entry.getCreationTime());
		assertEquals(this.originalLastAccessStartTime, entry.getLastAccessStartTime().get());
		assertEquals(this.originalLastAccessEndTime, entry.getLastAccessEndTime().get());
		assertEquals(this.originalTimeout, entry.getTimeout());
	}

	void verifyUpdatedState(SessionMetaDataEntry entry) {
		assertEquals(this.created, entry.getCreationTime());
		assertEquals(this.updatedLastAccessStartTime, entry.getLastAccessStartTime().get());
		assertEquals(this.updatedLastAccessEndTime, entry.getLastAccessEndTime().get());
		assertEquals(this.updatedTimeout, entry.getTimeout());
	}
}
