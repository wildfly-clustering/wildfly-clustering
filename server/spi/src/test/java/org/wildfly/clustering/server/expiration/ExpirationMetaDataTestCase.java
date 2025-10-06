/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.expiration;

import static org.assertj.core.api.Assertions.*;

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
		assertThat(metaData.isExpired()).isFalse();
		assertThat(metaData.isImmortal()).isTrue();
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
		assertThat(metaData.isExpired()).isFalse();
		assertThat(metaData.isImmortal()).isTrue();
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
		assertThat(metaData.isExpired()).isFalse();
		assertThat(metaData.isImmortal()).isTrue();
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
		assertThat(metaData.isImmortal()).isFalse();
		assertThat(metaData.isExpired()).isTrue();
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
		assertThat(metaData.isImmortal()).isFalse();
		assertThat(metaData.isExpired()).isFalse();
	}
}
