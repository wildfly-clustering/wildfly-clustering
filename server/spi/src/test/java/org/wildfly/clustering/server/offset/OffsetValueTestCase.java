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

		assertTrue(value.getOffset().isZero());
		assertSame(Duration.ZERO, value.getBasis());
		assertSame(Duration.ZERO, value.get());
		assertSame(backward, value.getOffset().apply(backward));
		assertSame(Duration.ZERO, value.getOffset().apply(Duration.ZERO));
		assertSame(forward, value.getOffset().apply(forward));

		value.set(forward);

		assertFalse(value.getOffset().isZero());
		assertSame(Duration.ZERO, value.getBasis());
		assertEquals(forward, value.get());
		assertEquals(Duration.ZERO, value.getOffset().apply(backward));
		assertEquals(forward, value.getOffset().apply(Duration.ZERO));

		OffsetValue<Duration> rebaseValue = value.rebase();

		assertTrue(rebaseValue.getOffset().isZero());
		assertEquals(forward, rebaseValue.getBasis());
		assertEquals(forward, rebaseValue.get());
		assertEquals(Duration.ZERO, rebaseValue.getOffset().apply(Duration.ZERO));
		assertEquals(forward, rebaseValue.getOffset().apply(forward));

		value.set(backward);

		assertFalse(value.getOffset().isZero());
		assertSame(Duration.ZERO, value.getBasis());
		assertEquals(backward, value.get());
		assertEquals(backward, value.getOffset().apply(Duration.ZERO));
		assertEquals(Duration.ZERO, value.getOffset().apply(forward));

		// Verify rebase offset value reflects change in basis, but with unchanged offset
		assertTrue(rebaseValue.getOffset().isZero());
		assertEquals(value.get(), rebaseValue.getBasis());
		assertEquals(value.get(), rebaseValue.get());
		assertEquals(Duration.ZERO, rebaseValue.getOffset().apply(Duration.ZERO));
		assertEquals(forward, rebaseValue.getOffset().apply(forward));

		// Test from positive duration
		value = OffsetValue.from(forward);

		assertTrue(value.getOffset().isZero());
		assertSame(forward, value.getBasis());
		assertSame(forward, value.get());
		assertSame(backward, value.getOffset().apply(backward));
		assertSame(Duration.ZERO, value.getOffset().apply(Duration.ZERO));
		assertEquals(forward, value.getOffset().apply(forward));

		value.set(Duration.ZERO);

		assertFalse(value.getOffset().isZero());
		assertSame(forward, value.getBasis());
		assertSame(Duration.ZERO, value.get());
		assertEquals(backward, value.getOffset().apply(Duration.ZERO));
		assertEquals(Duration.ZERO, value.getOffset().apply(forward));

		// Test negative duration
		value = OffsetValue.from(backward);

		assertTrue(value.getOffset().isZero());
		assertSame(backward, value.getBasis());
		assertSame(backward, value.get());
		assertSame(backward, value.getOffset().apply(backward));
		assertSame(Duration.ZERO, value.getOffset().apply(Duration.ZERO));
		assertEquals(forward, value.getOffset().apply(forward));

		value.set(Duration.ZERO);

		assertFalse(value.getOffset().isZero());
		assertSame(backward, value.getBasis());
		assertSame(Duration.ZERO, value.get());
		assertEquals(Duration.ZERO, value.getOffset().apply(backward));
		assertEquals(forward, value.getOffset().apply(Duration.ZERO));
	}

	@Test
	public void instant() {
		Duration forward = Duration.ofSeconds(1);
		Duration backward = Duration.ofSeconds(-1);
		Instant present = Instant.now();
		Instant past = present.plus(backward);
		Instant future = present.plus(forward);

		OffsetValue<Instant> value = OffsetValue.from(present);

		assertTrue(value.getOffset().isZero());
		assertSame(present, value.getBasis());
		assertSame(present, value.get());
		assertSame(past, value.getOffset().apply(past));
		assertSame(present, value.getOffset().apply(present));
		assertSame(future, value.getOffset().apply(future));

		value.set(future);

		assertFalse(value.getOffset().isZero());
		assertEquals(present, value.getBasis());
		assertEquals(future, value.get());
		assertEquals(present, value.getOffset().apply(past));
		assertEquals(future, value.getOffset().apply(present));

		OffsetValue<Instant> rebaseValue = value.rebase();

		assertTrue(rebaseValue.getOffset().isZero());
		assertEquals(future, rebaseValue.getBasis());
		assertEquals(future, rebaseValue.get());
		assertSame(past, rebaseValue.getOffset().apply(past));
		assertSame(present, rebaseValue.getOffset().apply(present));
		assertSame(future, rebaseValue.getOffset().apply(future));

		value.set(past);

		assertFalse(value.getOffset().isZero());
		assertEquals(present, value.getBasis());
		assertEquals(past, value.get());
		assertEquals(past, value.getOffset().apply(present));
		assertEquals(present, value.getOffset().apply(future));

		// Verify rebase offset value reflects change in basis, but with unchanged offset
		assertTrue(rebaseValue.getOffset().isZero());
		assertEquals(past, rebaseValue.getBasis());
		assertEquals(past, rebaseValue.get());
		assertSame(past, rebaseValue.getOffset().apply(past));
		assertSame(present, rebaseValue.getOffset().apply(present));
		assertSame(future, rebaseValue.getOffset().apply(future));
	}
}
