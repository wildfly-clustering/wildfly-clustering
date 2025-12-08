/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.expiration;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * Validates ExpirationMetaData logic.
 * @author Paul Ferraro
 */
public class ExpirationMetaDataTestCase {

	@Test
	public void immortal() {
		ExpirationMetaData metaData = new ExpirationMetaData() {
			@Override
			public Optional<Duration> getMaxIdle() {
				return Optional.empty();
			}

			@Override
			public Optional<Instant> getLastAccessTime() {
				return Optional.of(Instant.now());
			}
		};
		assertThat(metaData.isExpired()).isFalse();
		assertThat(metaData.getExpirationTime()).isEmpty();
	}

	@Test
	public void notYetAccessed() {
		ExpirationMetaData metaData = new ExpirationMetaData() {

			@Override
			public Optional<Duration> getMaxIdle() {
				return Optional.of(Duration.ofHours(1));
			}

			@Override
			public Optional<Instant> getLastAccessTime() {
				return Optional.empty();
			}
		};
		assertThat(metaData.isExpired()).isFalse();
		assertThat(metaData.getExpirationTime()).isEmpty();
	}

	@Test
	public void expired() {
		Duration maxIdle = Duration.ofHours(1);
		Instant expiryTime = Instant.now();
		Instant lastAccessed = expiryTime.minus(maxIdle);

		ExpirationMetaData metaData = new ExpirationMetaData() {
			@Override
			public Optional<Duration> getMaxIdle() {
				return Optional.of(maxIdle);
			}

			@Override
			public Optional<Instant> getLastAccessTime() {
				return Optional.of(lastAccessed);
			}
		};
		assertThat(metaData.isExpired()).isTrue();
		assertThat(metaData.getExpirationTime()).hasValue(expiryTime);
	}

	@Test
	public void notYetExpired() {
		Duration maxIdle = Duration.ofHours(1);
		Instant lastAccessed = Instant.now();
		Instant expiryTime = lastAccessed.plus(maxIdle);

		ExpirationMetaData metaData = new ExpirationMetaData() {
			@Override
			public Optional<Duration> getMaxIdle() {
				return Optional.of(maxIdle);
			}

			@Override
			public Optional<Instant> getLastAccessTime() {
				return Optional.of(lastAccessed);
			}
		};
		assertThat(metaData.isExpired()).isFalse();
		assertThat(metaData.getExpirationTime()).hasValue(expiryTime);
	}
}
