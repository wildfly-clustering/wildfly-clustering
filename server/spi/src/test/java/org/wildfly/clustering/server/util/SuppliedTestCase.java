/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link Supplied} implementations.
 * @author Paul Ferraro
 */
public class SuppliedTestCase {

	@Test
	public void cached() {
		String expected = "foo";
		String other = "bar";
		Supplier<String> factory = mock(Supplier.class);
		Supplied<String> supplied = Supplied.cached();

		when(factory.get()).thenReturn(expected, other);

		assertSame(expected, supplied.get(factory));

		verify(factory).get();

		assertSame(expected, supplied.get(factory));

		verifyNoMoreInteractions(factory);
	}

	@Test
	public void simple() {
		String expected = "foo";
		String other = "bar";
		Supplier<String> factory = mock(Supplier.class);
		Supplied<String> supplied = Supplied.simple();

		when(factory.get()).thenReturn(expected, other);

		assertSame(expected, supplied.get(factory));

		verify(factory).get();

		assertSame(other, supplied.get(factory));

		verify(factory, times(2)).get();
	}
}
