/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.embedded;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Predicate;
import org.wildfly.clustering.function.Supplier;
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
		ImmutableSessionMetaData immortalMetaData = mock(ImmutableSessionMetaData.class);
		ImmutableSession expiredSession = mock(ImmutableSession.class);

		String missingSessionId = "missing";
		String expiredSessionId = "expired";
		String validSessionId = "valid";
		String immortalSessionId = "immortal";

		UUID expiredMetaDataValue = UUID.randomUUID();
		UUID expiredAttributesValue = UUID.randomUUID();
		UUID validMetaDataValue = UUID.randomUUID();
		UUID immortalMetaDataValue = UUID.randomUUID();

		Predicate<String> task = new SessionExpirationTask<>(sessionFactory, batchFactory, listener);

		doReturn(metaDataFactory).when(sessionFactory).getSessionMetaDataFactory();
		doReturn(attributesFactory).when(sessionFactory).getSessionAttributesFactory();

		doReturn(null).when(metaDataFactory).tryValue(missingSessionId);
		doReturn(expiredMetaDataValue).when(metaDataFactory).tryValue(expiredSessionId);
		doReturn(validMetaDataValue).when(metaDataFactory).tryValue(validSessionId);
		doReturn(immortalMetaDataValue).when(metaDataFactory).tryValue(immortalSessionId);

		doReturn(expiredMetaData).when(metaDataFactory).createImmutableSessionMetaData(expiredSessionId, expiredMetaDataValue);
		doReturn(validMetaData).when(metaDataFactory).createImmutableSessionMetaData(validSessionId, validMetaDataValue);
		doReturn(immortalMetaData).when(metaDataFactory).createImmutableSessionMetaData(immortalSessionId, immortalMetaDataValue);

		doReturn(true).when(expiredMetaData).isExpired();
		doReturn(false).when(validMetaData).isExpired();
		doReturn(false).when(immortalMetaData).isExpired();

		doReturn(Optional.of(Instant.now().plus(Duration.ofMinutes(1)))).when(validMetaData).getExpirationTime();
		doReturn(Optional.empty()).when(immortalMetaData).getExpirationTime();

		doReturn(expiredAttributesValue).when(attributesFactory).findValue(expiredSessionId);
		doReturn(expiredAttributes).when(attributesFactory).createImmutableSessionAttributes(expiredSessionId, expiredAttributesValue);
		doReturn(expiredSession).when(sessionFactory).createImmutableSession(same(expiredSessionId), same(expiredMetaData), same(expiredAttributes));

		assertThat(task.test(missingSessionId)).isTrue();
		assertThat(task.test(expiredSessionId)).isTrue();
		assertThat(task.test(validSessionId)).isFalse();

		verify(sessionFactory).remove(expiredSessionId);
		verify(sessionFactory, never()).remove(missingSessionId);
		verify(sessionFactory, never()).remove(validSessionId);

		verify(listener).accept(expiredSession);
		verifyNoMoreInteractions(listener);
	}
}
