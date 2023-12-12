/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

/**
 * Abstract unit test for {@link SessionCreationMetaDataEntry} implementations.
 * @author Paul Ferraro
 */
public abstract class AbstractSessionCreationMetaDataEntryTestCase implements Consumer<SessionCreationMetaDataEntry<Object>> {

	private final Instant created = Instant.now();
	private final Duration originalTimeout = Duration.ofMinutes(20);

	private final Duration updatedTimeout = Duration.ofMinutes(30);

	@Test
	public void test() {
		DefaultSessionCreationMetaDataEntry<Object> entry = new DefaultSessionCreationMetaDataEntry<>(this.created);

		// Verify defaults
		assertEquals(this.created, entry.getCreationTime());
		assertEquals(Duration.ZERO, entry.getTimeout());
		assertNull(entry.getContext(() -> null));

		// Apply original state
		entry.setTimeout(this.originalTimeout);

		this.verifyOriginalState(entry);

		this.accept(entry);
	}

	void updateState(SessionCreationMetaData metaData) {
		metaData.setTimeout(this.updatedTimeout);
	}

	void verifyOriginalState(SessionCreationMetaData metaData) {
		assertEquals(this.created, metaData.getCreationTime());
		assertEquals(this.originalTimeout, metaData.getTimeout());
	}

	void verifyUpdatedState(SessionCreationMetaData metaData) {
		assertEquals(this.created, metaData.getCreationTime());
		assertEquals(this.updatedTimeout, metaData.getTimeout());
	}
}
