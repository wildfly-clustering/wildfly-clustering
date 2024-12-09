/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.offset;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link Offset}.
 * @author Paul Ferraro
 */
public class OffsetTestCase {

	@Test
	public void duration() {
		Duration forward = Duration.ofSeconds(1);
		Duration backward = Duration.ofSeconds(-1);

		// Test zero offset
		Offset<Duration> offset = Offset.forDuration(Duration.ZERO);

		assertThat(offset.apply(backward)).isSameAs(backward);
		assertThat(offset.apply(Duration.ZERO)).isSameAs(Duration.ZERO);
		assertThat(offset.apply(forward)).isSameAs(forward);

		// Test positive offset
		offset = Offset.forDuration(forward);

		assertThat(offset.apply(backward)).isZero();
		assertThat(offset.apply(Duration.ZERO)).isEqualTo(forward);

		// Test negative offset
		offset = Offset.forDuration(backward);

		assertThat(offset.apply(Duration.ZERO)).isEqualTo(backward);
		assertThat(offset.apply(forward)).isZero();
	}

	@Test
	public void instant() {
		Duration forward = Duration.ofSeconds(1);
		Duration backward = Duration.ofSeconds(-1);
		Instant present = Instant.now();
		Instant past = present.plus(backward);
		Instant future = present.plus(forward);

		// Test zero offset
		Offset<Instant> offset = Offset.forInstant(Duration.ZERO);

		assertThat(offset.apply(past)).isSameAs(past);
		assertThat(offset.apply(present)).isSameAs(present);
		assertThat(offset.apply(future)).isSameAs(future);

		// Test positive offset
		offset = Offset.forInstant(forward);

		assertThat(offset.apply(past)).isEqualTo(present);
		assertThat(offset.apply(present)).isEqualTo(future);

		// Test negative offset
		offset = Offset.forInstant(backward);

		assertThat(offset.apply(present)).isEqualTo(past);
		assertThat(offset.apply(future)).isEqualTo(present);
	}
}
