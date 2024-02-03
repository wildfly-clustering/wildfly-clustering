/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.fine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.session.cache.attributes.SessionAttributes;

/**
 * Unit test for {@link FineSessionAttributes}.
 * @author Paul Ferraro
 */
public class FineSessionAttributesTestCase {

	private final String id = "id";
	private final CacheEntryMutatorFactory<String, Map<String, Object>> mutatorFactory = mock(CacheEntryMutatorFactory.class);
	private final Marshaller<Object, Object> marshaller = mock(Marshaller.class);
	private final Immutability immutability = mock(Immutability.class);
	private final CacheProperties properties = mock(CacheProperties.class);
	private final SessionAttributeActivationNotifier notifier = mock(SessionAttributeActivationNotifier.class);

	private SessionAttributes createSessionAttributes(String id, Map<String, Object> map) {
		doReturn(true).when(this.properties).isMarshalling();

		SessionAttributes attribute = new FineSessionAttributes<>(this.id, new TreeMap<>(map), this.mutatorFactory, this.marshaller, this.immutability, this.properties, this.notifier);

		for (Object value : map.values()) {
			verify(this.notifier).postActivate(value);
		}

		return attribute;
	}

	@AfterEach
	public void resetMocks() {
		reset(this.mutatorFactory, this.marshaller, this.immutability, this.properties, this.notifier);
	}

	@Test
	public void getAttributeNames() {
		Map<String, Object> map = Map.of("foo", UUID.randomUUID(), "bar", UUID.randomUUID());
		try (SessionAttributes attributes = this.createSessionAttributes("id", map)) {
			assertEquals(map.keySet(), attributes.getAttributeNames());
		}

		for (Object value : map.values()) {
			verify(this.notifier).prePassivate(value);
		}

		verifyNoInteractions(this.properties);
		verifyNoInteractions(this.marshaller);
		verifyNoInteractions(this.mutatorFactory);
	}

	@Test
	public void getAttribute() throws IOException {
		UUID mutable = UUID.randomUUID();
		UUID immutable = UUID.randomUUID();
		Map<String, Object> map = Map.of("mutable", mutable, "immutable", immutable);

		doReturn(true).when(this.immutability).test(immutable);
		doReturn(false).when(this.immutability).test(mutable);

		// Verify read-only request
		try (SessionAttributes attributes = this.createSessionAttributes("id", map)) {
			// Verify non-existant attribute
			assertNull(attributes.getAttribute("missing"));

			// Verify immutable attributes
			assertSame(immutable, attributes.getAttribute("immutable"));
		}

		for (Object value : map.values()) {
			verify(this.notifier).prePassivate(value);
		}
		// Read-only request should not write
		verifyNoInteractions(this.mutatorFactory);

		UUID marshalledMutable = UUID.randomUUID();
		ArgumentCaptor<Map<String, Object>> capturedUpdates = ArgumentCaptor.captor();
		CacheEntryMutator mutator = mock(CacheEntryMutator.class);

		reset(this.notifier);

		try (SessionAttributes attributes = this.createSessionAttributes("id", map)) {
			// Verify non-existant attribute
			assertNull(attributes.getAttribute("missing"));

			// Verify mutable/immutable attributes
			doReturn(marshalledMutable).when(this.marshaller).write(mutable);
			doReturn(mutator).when(this.mutatorFactory).createMutator(eq("id"), capturedUpdates.capture());

			assertSame(immutable, attributes.getAttribute("immutable"));
			assertSame(mutable, attributes.getAttribute("mutable"));
		}

		for (Object value : map.values()) {
			verify(this.notifier).prePassivate(value);
		}

		// Accessing a mutable attribute should write
		verify(mutator).mutate();

		// Only mutable attributes should have been updated
		Map<String, Object> updates = capturedUpdates.getValue();
		assertEquals(Set.of("mutable"), updates.keySet());
		assertSame(marshalledMutable, updates.get("mutable"));
	}

	@Test
	public void removeAttribute() {
		UUID foo = UUID.randomUUID();
		UUID bar = UUID.randomUUID();
		try (SessionAttributes attributes = this.createSessionAttributes("id", Map.of("foo", foo, "bar", bar))) {
			// Verify non-existant attribute
			assertNull(attributes.removeAttribute("baz"));
		}

		verify(this.notifier).prePassivate(foo);
		verify(this.notifier).prePassivate(bar);

		verifyNoInteractions(this.mutatorFactory);

		reset(this.notifier);

		ArgumentCaptor<Map<String, Object>> capturedUpdates = ArgumentCaptor.captor();
		CacheEntryMutator mutator = mock(CacheEntryMutator.class);
		try (SessionAttributes attributes = this.createSessionAttributes("id", Map.of("foo", foo, "bar", bar))) {
			// Verify non-existant attribute
			assertNull(attributes.removeAttribute("baz"));

			// Verify mutable/immutable attributes
			doReturn(mutator).when(this.mutatorFactory).createMutator(eq("id"), capturedUpdates.capture());

			assertSame(foo, attributes.removeAttribute("foo"));
		}

		verify(this.notifier).prePassivate(bar);
		// There should be no pre-passivate event for removed attribute
		verify(this.notifier, never()).prePassivate(foo);

		verify(mutator).mutate();

		Map<String, Object> updates = capturedUpdates.getValue();
		assertEquals(Set.of("foo"), updates.keySet());
		assertNull(updates.get("foo"));
	}

	@Test
	public void setAttribute() throws IOException {
		UUID existing = UUID.randomUUID();
		UUID unmarshallable = UUID.randomUUID();

		try (SessionAttributes attributes = this.createSessionAttributes("id", Map.of("existing", existing))) {
			doReturn(false).when(this.marshaller).isMarshallable(unmarshallable);

			// Should be treated as a removal
			assertNull(attributes.setAttribute("missing", null));

			// Verify unmarshallable attribute
			assertThrows(IllegalArgumentException.class, () -> attributes.setAttribute("unmarshallable", unmarshallable));
		}

		verify(this.notifier).prePassivate(existing);
		// Nothing should have require writing
		verifyNoInteractions(this.mutatorFactory);

		reset(this.notifier);

		UUID removing = UUID.randomUUID();
		UUID newIntermediate = UUID.randomUUID();
		UUID newReplacement = UUID.randomUUID();
		UUID existingIntermediate = UUID.randomUUID();
		UUID existingReplacement = UUID.randomUUID();
		UUID marshalledNewReplacement = UUID.randomUUID();
		UUID marshalledExistingReplacement = UUID.randomUUID();
		ArgumentCaptor<Map<String, Object>> capturedUpdates = ArgumentCaptor.captor();
		CacheEntryMutator mutator = mock(CacheEntryMutator.class);
		try (SessionAttributes attributes = this.createSessionAttributes("id", Map.of("existing", existing, "removing", removing))) {
			doReturn(true).when(this.marshaller).isMarshallable(newIntermediate);
			doReturn(true).when(this.marshaller).isMarshallable(newReplacement);
			doReturn(true).when(this.marshaller).isMarshallable(existingIntermediate);
			doReturn(true).when(this.marshaller).isMarshallable(existingReplacement);

			// Verify new/updates attributes
			doReturn(mutator).when(this.mutatorFactory).createMutator(eq("id"), capturedUpdates.capture());
			doReturn(marshalledNewReplacement).when(this.marshaller).write(newReplacement);
			doReturn(marshalledExistingReplacement).when(this.marshaller).write(existingReplacement);

			// Test new attribute
			assertNull(attributes.setAttribute("new", newIntermediate));
			assertSame(newIntermediate, attributes.setAttribute("new", newReplacement));
			// Test replaced attribute
			assertSame(existing, attributes.setAttribute("existing", existingIntermediate));
			assertSame(existingIntermediate, attributes.setAttribute("existing", existingReplacement));
			// Should be treated as a removal
			assertSame(removing, attributes.setAttribute("removing", null));
		}

		verify(this.notifier).prePassivate(newReplacement);
		verify(this.notifier).prePassivate(existingReplacement);
		// There should be no pre-passivate events for replaced attributes
		verify(this.notifier, never()).prePassivate(newIntermediate);
		verify(this.notifier, never()).prePassivate(existing);
		verify(this.notifier, never()).prePassivate(existingIntermediate);

		Map<String, Object> updates = capturedUpdates.getValue();
		assertEquals(Set.of("new", "existing", "removing"), updates.keySet());
		assertSame(marshalledNewReplacement, updates.get("new"));
		assertSame(marshalledExistingReplacement, updates.get("existing"));
		assertNull(updates.get("removing"));
	}
}
