/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

/**
 * Abstract unit test for {@link SessionMetaDataEntry} implementations.
 * @author Paul Ferraro
 */
public abstract class AbstractSessionMetaDataEntryTestCase implements Consumer<ContextualSessionMetaDataEntry<Object>> {

	private final Instant originalLastAccessEndTime = Instant.now();
	private final Instant originalLastAccessStartTime = this.originalLastAccessEndTime.minus(Duration.ofSeconds(1));
	private final Instant created = this.originalLastAccessStartTime.minus(Duration.ofMinutes(1));
	private final Duration originalTimeout = Duration.ofMinutes(20);

	private final Instant updatedLastAccessStartTime = this.originalLastAccessEndTime.plus(Duration.ofSeconds(10));
	private final Instant updatedLastAccessEndTime = this.updatedLastAccessStartTime.plus(Duration.ofSeconds(2));
	private final Duration updatedTimeout = Duration.ofMinutes(30);

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
