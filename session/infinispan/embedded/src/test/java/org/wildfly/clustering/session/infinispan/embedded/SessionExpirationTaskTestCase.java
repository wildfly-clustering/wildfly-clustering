/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.embedded;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.SessionFactory;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactory;
import org.wildfly.clustering.session.cache.metadata.SessionMetaDataFactory;


/**
 * Unit test for {@link SessionExpirationTask}.
 *
 * @author Paul Ferraro
 */
public class SessionExpirationTaskTestCase {
	@Test
	public void test() {
		SessionFactory<Object, UUID, UUID, Object> sessionFactory = mock(SessionFactory.class);
		Supplier<Batch> batchFactory = mock(Supplier.class);
		SessionMetaDataFactory<UUID> metaDataFactory = mock(SessionMetaDataFactory.class);
		SessionAttributesFactory<Object, UUID> attributesFactory = mock(SessionAttributesFactory.class);
		Consumer<ImmutableSession> listener = mock(Consumer.class);
		Map<String, Object> expiredAttributes = mock(Map.class);
		ImmutableSessionMetaData validMetaData = mock(ImmutableSessionMetaData.class);
		ImmutableSessionMetaData expiredMetaData = mock(ImmutableSessionMetaData.class);
		ImmutableSession expiredSession = mock(ImmutableSession.class);

		String missingSessionId = "missing";
		String expiredSessionId = "expired";
		String validSessionId = "valid";

		UUID expiredMetaDataValue = UUID.randomUUID();
		UUID expiredAttributesValue = UUID.randomUUID();
		UUID validMetaDataValue = UUID.randomUUID();

		Predicate<String> task = new SessionExpirationTask<>(sessionFactory, batchFactory, listener);

		when(sessionFactory.getMetaDataFactory()).thenReturn(metaDataFactory);
		when(sessionFactory.getAttributesFactory()).thenReturn(attributesFactory);
		when(metaDataFactory.tryValue(missingSessionId)).thenReturn(null);
		when(metaDataFactory.tryValue(expiredSessionId)).thenReturn(expiredMetaDataValue);
		when(metaDataFactory.tryValue(validSessionId)).thenReturn(validMetaDataValue);

		when(metaDataFactory.createImmutableSessionMetaData(expiredSessionId, expiredMetaDataValue)).thenReturn(expiredMetaData);
		when(metaDataFactory.createImmutableSessionMetaData(validSessionId, validMetaDataValue)).thenReturn(validMetaData);

		when(expiredMetaData.isExpired()).thenReturn(true);
		when(validMetaData.isExpired()).thenReturn(false);

		when(attributesFactory.findValue(expiredSessionId)).thenReturn(expiredAttributesValue);
		when(attributesFactory.createImmutableSessionAttributes(expiredSessionId, expiredAttributesValue)).thenReturn(expiredAttributes);
		when(sessionFactory.createImmutableSession(same(expiredSessionId), same(expiredMetaData), same(expiredAttributes))).thenReturn(expiredSession);

		assertThat(task.test(missingSessionId)).isTrue();
		assertThat(task.test(expiredSessionId)).isTrue();
		assertThat(task.test(validSessionId)).isTrue();

		verify(sessionFactory).remove(expiredSessionId);
		verify(sessionFactory, never()).remove(missingSessionId);
		verify(sessionFactory, never()).remove(validSessionId);

		verify(listener).accept(expiredSession);
		verifyNoMoreInteractions(listener);
	}
}
