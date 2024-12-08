/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.util;

import static org.assertj.core.api.Assertions.*;
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

		assertThat(supplied.get(factory)).isSameAs(expected);

		verify(factory).get();

		assertThat(supplied.get(factory)).isSameAs(expected);

		verifyNoMoreInteractions(factory);
	}

	@Test
	public void simple() {
		String expected = "foo";
		String other = "bar";
		Supplier<String> factory = mock(Supplier.class);
		Supplied<String> supplied = Supplied.simple();

		when(factory.get()).thenReturn(expected, other);

		assertThat(supplied.get(factory)).isSameAs(expected);

		verify(factory).get();

		assertThat(supplied.get(factory)).isSameAs(other);

		verify(factory, times(2)).get();
	}
}
