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

	private SessionAttributes createSessionAttributes(Map<String, Object> map) {
		SessionAttributes attributes = new CoarseSessionAttributes(new TreeMap<>(map), this.mutator, this.marshallability, this.immutability, this.notifier);

		verify(this.notifier).postActivate();

		return attributes;
	}

	@Test
	public void removeAttribute() {
		UUID existing = UUID.randomUUID();
		try (SessionAttributes attributes = this.createSessionAttributes(Map.of("existing", existing))) {
			assertNull(attributes.removeAttribute("missing"));
		}

		// Nothing to mutate
		verifyNoInteractions(this.mutator);
		verify(this.notifier).prePassivate();
		reset(this.notifier);

		try (SessionAttributes attributes = this.createSessionAttributes(Map.of("existing", existing))) {
			assertNull(attributes.removeAttribute("missing"));
			assertSame(existing, attributes.removeAttribute("existing"));
		}

		verify(this.mutator).mutate();
		verify(this.notifier).prePassivate();
	}

	@Test
	public void setAttribute() {
		UUID existing = UUID.randomUUID();
		UUID unmarshallable = UUID.randomUUID();

		try (SessionAttributes attributes = this.createSessionAttributes(Map.of("existing", existing))) {
			doReturn(false).when(this.marshallability).isMarshallable(unmarshallable);

			// Should be treated as a removal
			assertNull(attributes.setAttribute("missing", null));

			// Verify unmarshallable attribute
			assertThrows(IllegalArgumentException.class, () -> attributes.setAttribute("unmarshallable", unmarshallable));
		}

		verify(this.notifier).prePassivate();
		// Nothing should have require writing
		verifyNoInteractions(this.mutator);
		reset(this.notifier);

		UUID removing = UUID.randomUUID();
		UUID newIntermediate = UUID.randomUUID();
		UUID newReplacement = UUID.randomUUID();
		UUID existingIntermediate = UUID.randomUUID();
		UUID existingReplacement = UUID.randomUUID();
		try (SessionAttributes attributes = this.createSessionAttributes(Map.of("existing", existing, "removing", removing))) {
			doReturn(true).when(this.marshallability).isMarshallable(newIntermediate);
			doReturn(true).when(this.marshallability).isMarshallable(newReplacement);
			doReturn(true).when(this.marshallability).isMarshallable(existingIntermediate);
			doReturn(true).when(this.marshallability).isMarshallable(existingReplacement);
			doReturn(false).when(this.marshallability).isMarshallable(unmarshallable);

			// Verify unmarshallable attribute
			assertThrows(IllegalArgumentException.class, () -> attributes.setAttribute("unmarshallable", unmarshallable));

			// Test new attribute
			assertNull(attributes.setAttribute("new", newIntermediate));
			assertSame(newIntermediate, attributes.setAttribute("new", newReplacement));
			// Test replaced attribute
			assertSame(existing, attributes.setAttribute("existing", existingIntermediate));
			assertSame(existingIntermediate, attributes.setAttribute("existing", existingReplacement));

			// Should be treated as a removal
			assertSame(removing, attributes.setAttribute("removing", null));
		}

		verify(this.notifier).prePassivate();
		verify(this.mutator).mutate();
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
		verifyNoInteractions(this.mutator);
		verify(this.notifier).prePassivate();
	}

	@Test
	public void getAttribute() {
		UUID mutable = UUID.randomUUID();
		UUID immutable = UUID.randomUUID();
		Map<String, Object> map = Map.of("mutable", mutable, "immutable", immutable);

		doReturn(true).when(this.immutability).test(immutable);
		doReturn(false).when(this.immutability).test(mutable);

		// Verify read-only request
		try (SessionAttributes attributes = this.createSessionAttributes(map)) {
			// Verify non-existant attribute
			assertNull(attributes.getAttribute("foo"));

			// Verify mutable/immutable attributes
			assertSame(immutable, attributes.getAttribute("immutable"));
		}

		verify(this.notifier).prePassivate();
		verifyNoInteractions(this.mutator);
		reset(this.notifier);

		try (SessionAttributes attributes = this.createSessionAttributes(map)) {
			// Verify non-existant attribute
			assertNull(attributes.getAttribute("baz"));

			// Verify mutable/immutable attributes
			assertSame(immutable, attributes.getAttribute("immutable"));
			assertSame(mutable, attributes.getAttribute("mutable"));
		}

		verify(this.notifier).prePassivate();
		// Accessing mutable attribute should write
		verify(this.mutator).mutate();
	}
}
