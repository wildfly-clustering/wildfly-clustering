/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.coarse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.marshalling.Marshallability;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.session.cache.attributes.SessionAttributes;

/**
 * @author Paul Ferraro
 */
public class CoarseSessionAttributesTestCase {

	private final CacheEntryMutator mutator = mock(CacheEntryMutator.class);
	private final Marshallability marshallability = mock(Marshallability.class);
	private final Immutability immutability = mock(Immutability.class);
	private final SessionActivationNotifier notifier = mock(SessionActivationNotifier.class);

	@AfterEach
	public void resetMocks() {
		reset(this.mutator, this.marshallability, this.immutability);
	}

	@Test
	public void removeAttribute() {
		Map<String, Object> values = new TreeMap<>();
		UUID foo = UUID.randomUUID();
		values.put("foo", foo);

		@SuppressWarnings("resource")
		SessionAttributes attributes = new CoarseSessionAttributes(values, this.mutator, this.marshallability, this.immutability, this.notifier);

		verify(this.notifier).postActivate();

		// Nothing removed
		attributes.close();

		verify(this.mutator, never()).mutate();
		verify(this.notifier).prePassivate();
		reset(this.notifier);

		assertNull(attributes.removeAttribute("bar"));

		// Nothing removed
		attributes.close();

		verify(this.mutator, never()).mutate();
		verify(this.immutability, never()).test("bar");
		verify(this.notifier).prePassivate();
		reset(this.notifier);

		assertSame(foo, attributes.removeAttribute("foo"));

		assertFalse(values.containsKey("foo"));

		verify(this.mutator, never()).mutate();
		verify(this.immutability, never()).test("foo");

		attributes.close();

		verify(this.mutator).mutate();
		verify(this.notifier).prePassivate();
	}

	@Test
	public void setAttribute() {
		Map<String, Object> values = new TreeMap<>();
		UUID marshallable = UUID.randomUUID();
		UUID nonMarshallable = UUID.randomUUID();

		@SuppressWarnings("resource")
		SessionAttributes attributes = new CoarseSessionAttributes(values, this.mutator, this.marshallability, this.immutability, this.notifier);

		verify(this.notifier).postActivate();

		when(this.marshallability.isMarshallable(marshallable)).thenReturn(true);
		when(this.marshallability.isMarshallable(nonMarshallable)).thenReturn(false);

		assertThrows(IllegalArgumentException.class, () -> attributes.setAttribute("foo", nonMarshallable));

		// Nothing changed
		attributes.close();

		verify(this.mutator, never()).mutate();
		verify(this.notifier).prePassivate();
		reset(this.notifier);

		assertNull(attributes.setAttribute("foo", marshallable));

		assertTrue(values.containsKey("foo"));

		verify(this.immutability, never()).test("foo");

		attributes.close();

		verify(this.mutator).mutate();
		verify(this.notifier).prePassivate();
		reset(this.mutator, this.notifier);

		assertSame(marshallable, attributes.setAttribute("foo", null));

		verify(this.mutator, never()).mutate();
		verify(this.immutability, never()).test("foo");

		attributes.close();

		verify(this.mutator).mutate();
		verify(this.notifier).prePassivate();
	}

	@Test
	public void getAttributeNames() {
		UUID foo = UUID.randomUUID();
		UUID bar = UUID.randomUUID();
		Map<String, Object> values = Map.of("foo", foo, "bar", bar);

		try (SessionAttributes attributes = new CoarseSessionAttributes(values, this.mutator, this.marshallability, this.immutability, this.notifier)) {
			Set<String> names = attributes.getAttributeNames();
			assertEquals(2, names.size());
			assertTrue(names.contains("foo"));
			assertTrue(names.contains("bar"));
			assertFalse(names.contains("baz"));
			assertThrows(UnsupportedOperationException.class, () -> names.add("baz"));
		}
		// Read-only operations do not require mutation
		verify(this.mutator, never()).mutate();
		verify(this.notifier).prePassivate();
	}

	@Test
	public void getAttribute() {
		UUID immutable = UUID.randomUUID();
		UUID mutable = UUID.randomUUID();
		Map<String, Object> values = Map.of("immutable", immutable, "mutable", mutable);

		@SuppressWarnings("resource")
		SessionAttributes attributes = new CoarseSessionAttributes(values, this.mutator, this.marshallability, this.immutability, this.notifier);

		when(this.immutability.test(null)).thenReturn(true);
		when(this.immutability.test(immutable)).thenReturn(true);
		when(this.immutability.test(mutable)).thenReturn(false);

		assertSame(immutable, attributes.getAttribute("immutable"));
		assertNull(attributes.getAttribute("foo"));

		verify(this.mutator, never()).mutate();

		attributes.close();

		verify(this.mutator, never()).mutate();
		verify(this.notifier).prePassivate();
		reset(this.notifier);

		assertSame(mutable, attributes.getAttribute("mutable"));

		verify(this.mutator, never()).mutate();

		attributes.close();

		// Mutable read triggers mutation on close
		verify(this.mutator).mutate();
		verify(this.notifier).prePassivate();
	}
}
