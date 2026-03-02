/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.coarse;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.function.Predicate;
import org.wildfly.clustering.session.cache.attributes.SessionAttributeActivationNotifier;
import org.wildfly.clustering.session.cache.attributes.SessionAttributes;

/**
 * @author Paul Ferraro
 */
public class CoarseSessionAttributesTestCase {

	@Test
	public void removeAttribute() {
		Runnable mutator = mock(Runnable.class);
		Predicate<Object> marshallable = mock(Predicate.class);
		Predicate<Object> immutable = mock(Predicate.class);
		SessionAttributeActivationNotifier notifier = mock(SessionAttributeActivationNotifier.class);

		UUID exists = UUID.randomUUID();
		Map<String, Object> map = new TreeMap<>(Map.of("exists", exists));

		try (SessionAttributes attributes = new CoarseSessionAttributes(map, mutator, marshallable, immutable, notifier)) {

			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verify(notifier).postActivate(exists);
			verifyNoMoreInteractions(notifier);

			assertThat(attributes.remove("exists")).isSameAs(exists);

			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verifyNoInteractions(mutator);
			verifyNoMoreInteractions(notifier);
		}

		verifyNoInteractions(marshallable);
		verifyNoInteractions(immutable);
		verify(mutator).run();
		verifyNoMoreInteractions(mutator);
		verifyNoMoreInteractions(notifier);
	}

	@Test
	public void removeMissingAttribute() {
		Runnable mutator = mock(Runnable.class);
		Predicate<Object> marshallable = mock(Predicate.class);
		Predicate<Object> immutable = mock(Predicate.class);
		SessionAttributeActivationNotifier notifier = mock(SessionAttributeActivationNotifier.class);

		Map<String, Object> map = new TreeMap<>();

		try (SessionAttributes attributes = new CoarseSessionAttributes(map, mutator, marshallable, immutable, notifier)) {

			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verifyNoInteractions(notifier);

			assertThat(attributes.remove("remove")).isNull();
			assertThat(attributes.put("put", null)).isNull();

			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verifyNoMoreInteractions(notifier);
		}

		verifyNoInteractions(mutator);
		verifyNoInteractions(marshallable);
		verifyNoInteractions(immutable);
		verifyNoInteractions(notifier);
	}

	@Test
	public void setAttribute() {
		Runnable mutator = mock(Runnable.class);
		Predicate<Object> marshallable = mock(Predicate.class);
		Predicate<Object> immutable = mock(Predicate.class);
		SessionAttributeActivationNotifier notifier = mock(SessionAttributeActivationNotifier.class);

		UUID initial = UUID.randomUUID();
		UUID replacement = UUID.randomUUID();
		Map<String, Object> map = new TreeMap<>();

		try (SessionAttributes attributes = new CoarseSessionAttributes(map, mutator, marshallable, immutable, notifier)) {

			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verifyNoInteractions(notifier);

			doReturn(true).when(marshallable).test(initial);
			doReturn(true).when(marshallable).test(replacement);

			assertThat(attributes.put("value", initial)).isNull();
			assertThat(attributes.put("value", replacement)).isSameAs(initial);

			verifyNoInteractions(mutator);
			verify(marshallable).test(initial);
			verify(marshallable).test(replacement);
			verifyNoMoreInteractions(marshallable);
			verifyNoInteractions(immutable);
			verifyNoMoreInteractions(notifier);
		}

		verify(mutator).run();
		verifyNoMoreInteractions(mutator);
		verifyNoMoreInteractions(marshallable);
		verifyNoInteractions(immutable);
		verify(notifier).prePassivate(replacement);
		verifyNoMoreInteractions(notifier);
	}

	@Test
	public void setUnmarshallableAttribute() {
		Map<String, Object> map = new TreeMap<>();
		Runnable mutator = mock(Runnable.class);
		Predicate<Object> marshallable = mock(Predicate.class);
		Predicate<Object> immutable = mock(Predicate.class);
		SessionAttributeActivationNotifier notifier = mock(SessionAttributeActivationNotifier.class);

		UUID unmarshallable = UUID.randomUUID();

		try (SessionAttributes attributes = new CoarseSessionAttributes(map, mutator, marshallable, immutable, notifier)) {

			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verifyNoInteractions(notifier);

			doReturn(false).when(marshallable).test(unmarshallable);

			assertThatThrownBy(() -> attributes.put("unmarshallable", unmarshallable)).isInstanceOf(IllegalArgumentException.class);

			verifyNoInteractions(mutator);
			verify(marshallable).test(unmarshallable);
			verifyNoMoreInteractions(marshallable);
			verifyNoInteractions(immutable);
			verifyNoMoreInteractions(notifier);
		}

		verifyNoInteractions(mutator);
		verifyNoMoreInteractions(marshallable);
		verifyNoInteractions(immutable);
		verifyNoInteractions(notifier);
	}

	@Test
	public void getAttributeNames() {
		Runnable mutator = mock(Runnable.class);
		Predicate<Object> marshallable = mock(Predicate.class);
		Predicate<Object> immutable = mock(Predicate.class);
		SessionAttributeActivationNotifier notifier = mock(SessionAttributeActivationNotifier.class);

		Map.Entry<String, Object> foo = Map.entry("foo", UUID.randomUUID());
		Map.Entry<String, Object> bar = Map.entry("bar", UUID.randomUUID());
		Map<String, Object> map = Map.ofEntries(foo, bar);

		try (SessionAttributes attributes = new CoarseSessionAttributes(map, mutator, marshallable, immutable, notifier)) {

			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verify(notifier).postActivate(foo.getValue());
			verify(notifier).postActivate(bar.getValue());
			verifyNoMoreInteractions(notifier);

			assertThat(attributes.keySet()).containsExactlyInAnyOrderElementsOf(map.keySet());

			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verifyNoMoreInteractions(notifier);
		}

		verifyNoInteractions(mutator);
		verifyNoInteractions(marshallable);
		verifyNoInteractions(immutable);
		verify(notifier).prePassivate(foo.getValue());
		verify(notifier).prePassivate(bar.getValue());
		verifyNoMoreInteractions(notifier);
	}

	@Test
	public void getMutableAttribute() {
		UUID exists = UUID.randomUUID();
		Map<String, Object> map = new TreeMap<>(Map.of("exists", exists));
		Runnable mutator = mock(Runnable.class);
		Predicate<Object> marshallable = mock(Predicate.class);
		Predicate<Object> immutable = mock(Predicate.class);
		SessionAttributeActivationNotifier notifier = mock(SessionAttributeActivationNotifier.class);

		try (SessionAttributes attributes = new CoarseSessionAttributes(map, mutator, marshallable, immutable, notifier)) {

			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verify(notifier).postActivate(exists);
			verifyNoMoreInteractions(notifier);

			doReturn(false).when(immutable).test(exists);

			assertThat(attributes.get("exists")).isSameAs(exists);
			assertThat(attributes.get("exists")).isSameAs(exists);
			assertThat(attributes.get("missing")).isNull();

			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verify(immutable).test(exists);
			// Verify no redundant immutability checks
			verifyNoMoreInteractions(immutable);
			verifyNoMoreInteractions(notifier);
		}

		// Session should be dirty
		verify(mutator).run();
		verifyNoMoreInteractions(mutator);
		verifyNoInteractions(marshallable);
		verifyNoMoreInteractions(immutable);
		verify(notifier).prePassivate(exists);
		verifyNoMoreInteractions(notifier);
	}

	@Test
	public void getImmutableAttribute() {
		UUID exists = UUID.randomUUID();
		Map<String, Object> map = new TreeMap<>(Map.of("exists", exists));
		Runnable mutator = mock(Runnable.class);
		Predicate<Object> marshallable = mock(Predicate.class);
		Predicate<Object> immutable = mock(Predicate.class);
		SessionAttributeActivationNotifier notifier = mock(SessionAttributeActivationNotifier.class);

		try (SessionAttributes attributes = new CoarseSessionAttributes(map, mutator, marshallable, immutable, notifier)) {

			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verify(notifier).postActivate(exists);
			verifyNoMoreInteractions(notifier);

			doReturn(true).when(immutable).test(exists);

			assertThat(attributes.get("exists")).isSameAs(exists);
			assertThat(attributes.get("missing")).isNull();

			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verify(immutable).test(exists);
			verifyNoMoreInteractions(immutable);
			verifyNoMoreInteractions(notifier);
		}

		// Session should not be dirty
		verifyNoInteractions(mutator);
		verifyNoInteractions(marshallable);
		verifyNoMoreInteractions(immutable);
		verify(notifier).prePassivate(exists);
		verifyNoMoreInteractions(notifier);
	}
}
