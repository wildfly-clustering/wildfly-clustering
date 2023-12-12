/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.expiration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * Validates ExpirationMetaData logic.
 * @author Paul Ferraro
 */
public class ExpirationMetaDataTestCase {

	@Test
	public void nullTimeout() {
		ExpirationMetaData metaData = new ExpirationMetaData() {

			@Override
			public Duration getTimeout() {
				return null;
			}

			@Override
			public Instant getLastAccessTime() {
				return Instant.now().plus(Duration.ofHours(1));
			}
		};
		assertFalse(metaData.isExpired());
		assertTrue(metaData.isImmortal());
	}

	@Test
	public void negativeTimeout() {
		ExpirationMetaData metaData = new ExpirationMetaData() {

			@Override
			public Duration getTimeout() {
				return Duration.ofSeconds(-1);
			}

			@Override
			public Instant getLastAccessTime() {
				return Instant.now().plus(Duration.ofHours(1));
			}
		};
		assertFalse(metaData.isExpired());
		assertTrue(metaData.isImmortal());
	}

	@Test
	public void zeroTimeout() {
		ExpirationMetaData metaData = new ExpirationMetaData() {

			@Override
			public Duration getTimeout() {
				return Duration.ZERO;
			}

			@Override
			public Instant getLastAccessTime() {
				return Instant.now().plus(Duration.ofHours(1));
			}
		};
		assertFalse(metaData.isExpired());
		assertTrue(metaData.isImmortal());
	}

	@Test
	public void expired() {
		ExpirationMetaData metaData = new ExpirationMetaData() {

			@Override
			public Duration getTimeout() {
				return Duration.ofMinutes(1);
			}

			@Override
			public Instant getLastAccessTime() {
				return Instant.now().minus(Duration.ofHours(1));
			}
		};
		assertTrue(metaData.isExpired());
		assertFalse(metaData.isImmortal());
	}

	@Test
	public void notYetExpired() {
		ExpirationMetaData metaData = new ExpirationMetaData() {

			@Override
			public Duration getTimeout() {
				return Duration.ofHours(1);
			}

			@Override
			public Instant getLastAccessTime() {
				return Instant.now();
			}
		};
		assertFalse(metaData.isExpired());
		assertFalse(metaData.isImmortal());
	}
}
