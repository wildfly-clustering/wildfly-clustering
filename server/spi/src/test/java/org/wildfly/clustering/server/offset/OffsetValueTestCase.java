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
 * Unit test for {@link OffsetValue}.
 * @author Paul Ferraro
 */
public class OffsetValueTestCase {

	@Test
	public void duration() {
		Duration forward = Duration.ofSeconds(1);
		Duration backward = Duration.ofSeconds(-1);

		// Test from zero duration
		OffsetValue<Duration> value = OffsetValue.from(Duration.ZERO);

		assertThat(value.getOffset().isZero()).isTrue();
		assertThat(value.getBasis()).isSameAs(Duration.ZERO);
		assertThat(value.get()).isSameAs(Duration.ZERO);
		assertThat(value.getOffset().apply(backward)).isSameAs(backward);
		assertThat(value.getOffset().apply(Duration.ZERO)).isSameAs(Duration.ZERO);
		assertThat(value.getOffset().apply(forward)).isSameAs(forward);

		value.set(forward);

		assertThat(value.getOffset().isZero()).isFalse();
		assertThat(value.getBasis()).isSameAs(Duration.ZERO);
		assertThat(value.get()).isEqualTo(forward);
		assertThat(value.getOffset().apply(backward)).isZero();
		assertThat(value.getOffset().apply(Duration.ZERO)).isEqualTo(forward);

		OffsetValue<Duration> rebaseValue = value.rebase();

		assertThat(rebaseValue.getOffset().isZero()).isTrue();
		assertThat(rebaseValue.getBasis()).isEqualTo(forward);
		assertThat(rebaseValue.get()).isEqualTo(forward);
		assertThat(rebaseValue.getOffset().apply(Duration.ZERO)).isZero();
		assertThat(rebaseValue.getOffset().apply(forward)).isEqualTo(forward);

		value.set(backward);

		assertThat(value.getOffset().isZero()).isFalse();
		assertThat(value.getBasis()).isSameAs(Duration.ZERO);
		assertThat(value.get()).isEqualTo(backward);
		assertThat(value.getOffset().apply(Duration.ZERO)).isEqualTo(backward);
		assertThat(value.getOffset().apply(forward)).isZero();

		// Verify rebase offset value reflects change in basis, but with unchanged offset
		assertThat(rebaseValue.getOffset().isZero()).isTrue();
		assertThat(rebaseValue.getBasis()).isEqualTo(value.get());
		assertThat(rebaseValue.get()).isEqualTo(value.get());
		assertThat(rebaseValue.getOffset().apply(Duration.ZERO)).isZero();
		assertThat(rebaseValue.getOffset().apply(forward)).isEqualTo(forward);

		// Test from positive duration
		value = OffsetValue.from(forward);

		assertThat(value.getOffset().isZero()).isTrue();
		assertThat(value.getBasis()).isSameAs(forward);
		assertThat(value.get()).isSameAs(forward);
		assertThat(value.getOffset().apply(backward)).isSameAs(backward);
		assertThat(value.getOffset().apply(Duration.ZERO)).isSameAs(Duration.ZERO);
		assertThat(value.getOffset().apply(forward)).isEqualTo(forward);

		value.set(Duration.ZERO);

		assertThat(value.getOffset().isZero()).isFalse();
		assertThat(value.getBasis()).isSameAs(forward);
		assertThat(value.get()).isSameAs(Duration.ZERO);
		assertThat(value.getOffset().apply(Duration.ZERO)).isEqualTo(backward);
		assertThat(value.getOffset().apply(forward)).isZero();

		// Test negative duration
		value = OffsetValue.from(backward);

		assertThat(value.getOffset().isZero()).isTrue();
		assertThat(value.getBasis()).isSameAs(backward);
		assertThat(value.get()).isSameAs(backward);
		assertThat(value.getOffset().apply(backward)).isSameAs(backward);
		assertThat(value.getOffset().apply(Duration.ZERO)).isSameAs(Duration.ZERO);
		assertThat(value.getOffset().apply(forward)).isEqualTo(forward);

		value.set(Duration.ZERO);

		assertThat(value.getOffset().isZero()).isFalse();
		assertThat(value.getBasis()).isSameAs(backward);
		assertThat(value.get()).isSameAs(Duration.ZERO);
		assertThat(value.getOffset().apply(backward)).isZero();
		assertThat(value.getOffset().apply(Duration.ZERO)).isEqualTo(forward);
	}

	@Test
	public void instant() {
		Duration forward = Duration.ofSeconds(1);
		Duration backward = Duration.ofSeconds(-1);
		Instant present = Instant.now();
		Instant past = present.plus(backward);
		Instant future = present.plus(forward);

		OffsetValue<Instant> value = OffsetValue.from(present);

		assertThat(value.getOffset().isZero()).isTrue();
		assertThat(value.getBasis()).isSameAs(present);
		assertThat(value.get()).isSameAs(present);
		assertThat(value.getOffset().apply(past)).isSameAs(past);
		assertThat(value.getOffset().apply(present)).isSameAs(present);
		assertThat(value.getOffset().apply(future)).isSameAs(future);

		value.set(future);

		assertThat(value.getOffset().isZero()).isFalse();
		assertThat(value.getBasis()).isEqualTo(present);
		assertThat(value.get()).isEqualTo(future);
		assertThat(value.getOffset().apply(past)).isEqualTo(present);
		assertThat(value.getOffset().apply(present)).isEqualTo(future);

		OffsetValue<Instant> rebaseValue = value.rebase();

		assertThat(rebaseValue.getOffset().isZero()).isTrue();
		assertThat(rebaseValue.getBasis()).isEqualTo(future);
		assertThat(rebaseValue.get()).isEqualTo(future);
		assertThat(rebaseValue.getOffset().apply(past)).isSameAs(past);
		assertThat(rebaseValue.getOffset().apply(present)).isSameAs(present);
		assertThat(rebaseValue.getOffset().apply(future)).isSameAs(future);

		value.set(past);

		assertThat(value.getOffset().isZero()).isFalse();
		assertThat(value.getBasis()).isEqualTo(present);
		assertThat(value.get()).isEqualTo(past);
		assertThat(value.getOffset().apply(present)).isEqualTo(past);
		assertThat(value.getOffset().apply(future)).isEqualTo(present);

		// Verify rebase offset value reflects change in basis, but with unchanged offset
		assertThat(rebaseValue.getOffset().isZero()).isTrue();
		assertThat(rebaseValue.getBasis()).isEqualTo(past);
		assertThat(rebaseValue.get()).isEqualTo(past);
		assertThat(rebaseValue.getOffset().apply(past)).isSameAs(past);
		assertThat(rebaseValue.getOffset().apply(present)).isSameAs(present);
		assertThat(rebaseValue.getOffset().apply(future)).isSameAs(future);
	}
}
