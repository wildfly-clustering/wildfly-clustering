/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

/**
 * Abstract unit test for {@link SessionAccessMetaDataEntry} implementations.
 * @author Paul Ferraro
 */
public abstract class AbstractSessionAccessMetaDataEntryTestCase implements Consumer<SessionAccessMetaDataEntry> {

	private final Duration originalSinceCreation =  Duration.ofMinutes(1);
	private final Duration originalLastAccess = Duration.ofSeconds(1);

	private final Duration updatedSinceCreation = Duration.ofMinutes(2);
	private final Duration updatedLastAccess = Duration.ofSeconds(2);

	@Test
	public void test() {
		DefaultSessionAccessMetaDataEntry entry = new DefaultSessionAccessMetaDataEntry();

		// Verify defaults
		assertTrue(entry.getSinceCreationDuration().isZero());
		assertTrue(entry.getLastAccessDuration().isZero());

		// Apply original state
		entry.setLastAccessDuration(this.originalSinceCreation, this.originalLastAccess);

		this.verifyOriginalState(entry);

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
