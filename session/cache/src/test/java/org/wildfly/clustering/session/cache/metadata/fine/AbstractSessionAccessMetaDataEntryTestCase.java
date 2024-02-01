/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.offset.Offset;

/**
 * Abstract unit test for {@link SessionAccessMetaDataEntry} implementations.
 * @author Paul Ferraro
 */
public abstract class AbstractSessionAccessMetaDataEntryTestCase implements Consumer<SessionAccessMetaDataEntry> {

	private final Duration originalSinceCreation =  Duration.ofMinutes(1);
	private final Duration originalLastAccess = Duration.ofSeconds(1);

	private final Duration sinceCreationDelta = Duration.ofMinutes(1);
	private final Duration lastAccessDelta = Duration.ofSeconds(1);

	private final Duration updatedSinceCreation = this.originalSinceCreation.plus(this.sinceCreationDelta);
	private final Duration updatedLastAccess = this.originalLastAccess.plus(this.lastAccessDelta);

	@Test
	public void test() {
		DefaultSessionAccessMetaDataEntry entry = new DefaultSessionAccessMetaDataEntry();

		// Verify defaults
		assertTrue(entry.getSinceCreationDuration().isZero());
		assertTrue(entry.getLastAccessDuration().isZero());

		// Apply original state
		entry.setLastAccessDuration(this.originalSinceCreation, this.originalLastAccess);

		this.verifyOriginalState(entry);

		// Verify remap
		SessionAccessMetaDataEntryOffsets offsets = mock(SessionAccessMetaDataEntryOffsets.class);
		doReturn(Offset.forDuration(this.sinceCreationDelta)).when(offsets).getSinceCreationOffset();
		doReturn(Offset.forDuration(this.lastAccessDelta)).when(offsets).getLastAccessOffset();

		SessionAccessMetaDataEntry remapped = entry.remap(offsets);

		this.verifyUpdatedState(remapped);
		// Verify that remap is side-effect free
		this.verifyOriginalState(entry);

		// Implementation-specific validation
		this.accept(entry);
	}

	void updateState(SessionAccessMetaData entry) {
		entry.setLastAccessDuration(this.updatedSinceCreation, this.updatedLastAccess);
	}

	void verifyOriginalState(SessionAccessMetaData metaData) {
		assertEquals(this.originalSinceCreation, metaData.getSinceCreationDuration());
		assertEquals(this.originalLastAccess, metaData.getLastAccessDuration());
	}

	void verifyUpdatedState(SessionAccessMetaData metaData) {
		assertEquals(this.updatedSinceCreation, metaData.getSinceCreationDuration());
		assertEquals(this.updatedLastAccess, metaData.getLastAccessDuration());
	}
}
