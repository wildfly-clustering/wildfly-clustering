/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.offset;

import static org.junit.jupiter.api.Assertions.*;

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

		assertSame(backward, offset.apply(backward));
		assertSame(Duration.ZERO, offset.apply(Duration.ZERO));
		assertSame(forward, offset.apply(forward));

		// Test positive offset
		offset = Offset.forDuration(forward);

		assertEquals(Duration.ZERO, offset.apply(backward));
		assertEquals(forward, offset.apply(Duration.ZERO));

		// Test negative offset
		offset = Offset.forDuration(backward);

		assertEquals(backward, offset.apply(Duration.ZERO));
		assertEquals(Duration.ZERO, offset.apply(forward));
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

		assertSame(past, offset.apply(past));
		assertSame(present, offset.apply(present));
		assertSame(future, offset.apply(future));

		// Test positive offset
		offset = Offset.forInstant(forward);

		assertEquals(present, offset.apply(past));
		assertEquals(future, offset.apply(present));

		// Test negative offset
		offset = Offset.forInstant(backward);

		assertEquals(past, offset.apply(present));
		assertEquals(present, offset.apply(future));
	}
}
