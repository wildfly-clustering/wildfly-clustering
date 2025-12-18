/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.coarse;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
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
		Map<String, Object> map = mock(Map.class);
		Runnable mutator = mock(Runnable.class);
		Predicate<Object> marshallable = mock(Predicate.class);
		Predicate<Object> immutable = mock(Predicate.class);
		SessionAttributeActivationNotifier notifier = mock(SessionAttributeActivationNotifier.class);

		UUID existing = UUID.randomUUID();

		doReturn(List.of(existing), List.of()).when(map).values();

		try (SessionAttributes attributes = new CoarseSessionAttributes(map, mutator, marshallable, immutable, notifier)) {

			verify(map).values();
			verifyNoMoreInteractions(map);
			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verify(notifier).postActivate(existing);
			verifyNoMoreInteractions(notifier);

			doReturn(existing).when(map).remove("remove");

			assertThat(attributes.remove("remove")).isSameAs(existing);

			verify(map).remove("remove");
			verifyNoMoreInteractions(map);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verifyNoInteractions(mutator);
			verifyNoMoreInteractions(notifier);
		} finally {
			verify(map, times(2)).values();
			verifyNoMoreInteractions(map);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verify(mutator).run();
			verifyNoMoreInteractions(mutator);
			verifyNoMoreInteractions(notifier);
		}
	}

	@Test
	public void removeMissingAttribute() {
		Map<String, Object> map = mock(Map.class);
		Runnable mutator = mock(Runnable.class);
		Predicate<Object> marshallable = mock(Predicate.class);
		Predicate<Object> immutable = mock(Predicate.class);
		SessionAttributeActivationNotifier notifier = mock(SessionAttributeActivationNotifier.class);

		doReturn(List.of()).when(map).values();

		try (SessionAttributes attributes = new CoarseSessionAttributes(map, mutator, marshallable, immutable, notifier)) {

			verify(map).values();
			verifyNoMoreInteractions(map);
			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verifyNoInteractions(notifier);

			assertThat(attributes.remove("remove")).isNull();
			assertThat(attributes.put("put", null)).isNull();

			verify(map).remove("remove");
			verify(map).remove("put");
			verifyNoMoreInteractions(map);
			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verifyNoMoreInteractions(notifier);
		} finally {
			verify(map, times(2)).values();
			verifyNoMoreInteractions(map);
			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verifyNoInteractions(notifier);
		}
	}

	@Test
	public void setAttribute() {
		Map<String, Object> map = mock(Map.class);
		Runnable mutator = mock(Runnable.class);
		Predicate<Object> marshallable = mock(Predicate.class);
		Predicate<Object> immutable = mock(Predicate.class);
		SessionAttributeActivationNotifier notifier = mock(SessionAttributeActivationNotifier.class);

		UUID initial = UUID.randomUUID();
		UUID replacement = UUID.randomUUID();

		doReturn(List.of(initial), List.of(replacement)).when(map).values();

		try (SessionAttributes attributes = new CoarseSessionAttributes(map, mutator, marshallable, immutable, notifier)) {

			verify(map).values();
			verifyNoMoreInteractions(map);
			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verify(notifier).postActivate(initial);
			verifyNoMoreInteractions(notifier);

			doReturn(true).when(marshallable).test(initial);
			doReturn(true).when(marshallable).test(replacement);
			doReturn(initial).when(map).put("value", replacement);

			assertThat(attributes.put("value", initial)).isNull();
			assertThat(attributes.put("value", replacement)).isSameAs(initial);

			verify(map).put("value", initial);
			verify(map).put("value", replacement);
			verifyNoMoreInteractions(map);
			verifyNoInteractions(mutator);
			verify(marshallable).test(initial);
			verify(marshallable).test(replacement);
			verifyNoMoreInteractions(marshallable);
			verifyNoInteractions(immutable);
			verifyNoMoreInteractions(notifier);
		} finally {
			verify(map, times(2)).values();
			verifyNoMoreInteractions(map);
			verify(mutator).run();
			verifyNoMoreInteractions(mutator);
			verifyNoMoreInteractions(marshallable);
			verifyNoInteractions(immutable);
			verify(notifier).prePassivate(replacement);
			verifyNoMoreInteractions(notifier);
		}
	}

	@Test
	public void setUnmarshallableAttribute() {
		Map<String, Object> map = mock(Map.class);
		Runnable mutator = mock(Runnable.class);
		Predicate<Object> marshallable = mock(Predicate.class);
		Predicate<Object> immutable = mock(Predicate.class);
		SessionAttributeActivationNotifier notifier = mock(SessionAttributeActivationNotifier.class);

		UUID unmarshallable = UUID.randomUUID();

		doReturn(List.of()).when(map).values();

		try (SessionAttributes attributes = new CoarseSessionAttributes(map, mutator, marshallable, immutable, notifier)) {

			verify(map).values();
			verifyNoMoreInteractions(map);
			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verifyNoInteractions(notifier);

			doReturn(false).when(marshallable).test(unmarshallable);

			assertThatThrownBy(() -> attributes.put("unmarshallable", unmarshallable)).isInstanceOf(IllegalArgumentException.class);

			verifyNoMoreInteractions(map);
			verifyNoInteractions(mutator);
			verify(marshallable).test(unmarshallable);
			verifyNoMoreInteractions(marshallable);
			verifyNoInteractions(immutable);
			verifyNoMoreInteractions(notifier);
		} finally {
			verify(map, times(2)).values();
			verifyNoMoreInteractions(map);
			verifyNoInteractions(mutator);
			verifyNoMoreInteractions(marshallable);
			verifyNoInteractions(immutable);
			verifyNoInteractions(notifier);
		}
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
		} finally {
			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verify(notifier).prePassivate(foo.getValue());
			verify(notifier).prePassivate(bar.getValue());
			verifyNoMoreInteractions(notifier);
		}
	}

	@Test
	public void getMutableAttribute() {
		Map<String, Object> map = mock(Map.class);
		Runnable mutator = mock(Runnable.class);
		Predicate<Object> marshallable = mock(Predicate.class);
		Predicate<Object> immutable = mock(Predicate.class);
		SessionAttributeActivationNotifier notifier = mock(SessionAttributeActivationNotifier.class);

		UUID existent = UUID.randomUUID();
		UUID another = UUID.randomUUID();

		doReturn(List.of(existent, another)).when(map).values();

		try (SessionAttributes attributes = new CoarseSessionAttributes(map, mutator, marshallable, immutable, notifier)) {

			verify(map).values();
			verifyNoMoreInteractions(map);
			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verify(notifier).postActivate(existent);
			verify(notifier).postActivate(another);
			verifyNoMoreInteractions(notifier);

			doReturn(existent).when(map).get("existing");
			doReturn(another).when(map).get("another");

			assertThat(attributes.get("existing")).isSameAs(existent);
			assertThat(attributes.get("another")).isSameAs(another);
			assertThat(attributes.get("missing")).isNull();

			verify(map).get("existing");
			verify(map).get("another");
			verify(map).get("missing");
			verifyNoMoreInteractions(map);
			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verify(immutable).test(existent);
			// Verify no redundant immutability checks
			verifyNoMoreInteractions(immutable);
			verifyNoMoreInteractions(notifier);
		} finally {
			verify(map, times(2)).values();
			verifyNoMoreInteractions(map);
			verify(mutator).run();
			verifyNoMoreInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoMoreInteractions(immutable);
			verify(notifier).prePassivate(existent);
			verify(notifier).prePassivate(another);
			verifyNoMoreInteractions(notifier);
		}
	}

	@Test
	public void getImmutableAttribute() {
		Map<String, Object> map = mock(Map.class);
		Runnable mutator = mock(Runnable.class);
		Predicate<Object> marshallable = mock(Predicate.class);
		Predicate<Object> immutable = mock(Predicate.class);
		SessionAttributeActivationNotifier notifier = mock(SessionAttributeActivationNotifier.class);

		UUID existent = UUID.randomUUID();

		doReturn(List.of(existent)).when(map).values();

		try (SessionAttributes attributes = new CoarseSessionAttributes(map, mutator, marshallable, immutable, notifier)) {

			verify(map).values();
			verifyNoMoreInteractions(map);
			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoInteractions(immutable);
			verify(notifier).postActivate(existent);
			verifyNoMoreInteractions(notifier);

			doReturn(existent).when(map).get("existing");
			doReturn(true).when(immutable).test(existent);

			assertThat(attributes.get("existing")).isSameAs(existent);
			assertThat(attributes.get("missing")).isNull();

			verify(map).get("existing");
			verify(map).get("missing");
			verifyNoMoreInteractions(map);
			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verify(immutable).test(existent);
			verifyNoMoreInteractions(immutable);
			verifyNoMoreInteractions(notifier);
		} finally {
			verify(map, times(2)).values();
			verifyNoMoreInteractions(map);
			verifyNoInteractions(mutator);
			verifyNoInteractions(marshallable);
			verifyNoMoreInteractions(immutable);
			verify(notifier).prePassivate(existent);
			verifyNoMoreInteractions(notifier);
		}
	}
}
