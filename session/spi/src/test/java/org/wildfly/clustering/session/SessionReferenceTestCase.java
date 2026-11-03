/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.server.util.Reference;

/**
 * Unit test for {@link SessionManager#getSessionReference(String)}.
 * @author Paul Ferraro
 */
public class SessionReferenceTestCase {

	@Test
	public void test() {
		SessionManager<Void> manager = mock(SessionManager.class);
		String id = "foo";

		doCallRealMethod().when(manager).getSessionReference(any());

		Reference<Session<Void>> reference = manager.getSessionReference(id);
		Reference.Reader<Session<Void>> sessionReader = reference.getReader();

		assertThat(sessionReader.get()).isNull();

		Consumer<Session<Void>> sessionConsumer = mock(Consumer.class);

		sessionReader.read(sessionConsumer);

		verify(sessionConsumer).accept(null);

		Reference.Reader<Map<String, Object>> sessionAttributesReader = reference.getReader().map(Function.when(Objects::nonNull, Session::getAttributes, Function.of(null)));

		assertThat(sessionAttributesReader.get()).isNull();

		Session<Void> session = mock(Session.class);
		Map<String, Object> attributes = Map.of("bar", UUID.randomUUID());

		doReturn(session).when(manager).findSession(id);
		doReturn(attributes).when(session).getAttributes();

		assertThat(sessionReader.get()).isSameAs(session);

		sessionReader.read(sessionConsumer);

		verify(sessionConsumer).accept(session);

		assertThat(sessionAttributesReader.get()).isSameAs(attributes);

		Consumer<Map<String, Object>> attributesConsumer = mock(Consumer.class);

		sessionAttributesReader.read(attributesConsumer);

		verify(attributesConsumer).accept(attributes);
	}
}
