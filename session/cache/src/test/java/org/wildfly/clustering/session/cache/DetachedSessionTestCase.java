/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionMetaData;

/**
 * Unit test for {@link DetachedSession}.
 * @author Paul Ferraro
 */
public class DetachedSessionTestCase {
	private final String id = UUID.randomUUID().toString();
	private final Object localContext = new Object();
	private final Random random = new Random();

	@Test
	public void getId() {
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = mock(Supplier.class);

		doReturn(batchFactory).when(manager).getBatchFactory();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			assertThat(detached.getId()).isSameAs(this.id);
		}

		verifyNoInteractions(batchFactory);
	}

	@Test
	public void isValid() {
		Batch batch1 = mock(Batch.class);
		Batch batch2 = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = mock(Supplier.class);

		doReturn(batchFactory).when(manager).getBatchFactory();
		doReturn(batch1, batch2).when(batchFactory).get();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			doReturn(null).when(manager).findImmutableSession(this.id);

			assertThat(detached.isValid()).isFalse();

			verify(batchFactory).get();
			verify(batch1).close();
			verifyNoInteractions(batch2);

			ImmutableSession session = mock(ImmutableSession.class);

			doReturn(session).when(manager).findImmutableSession(this.id);

			assertThat(detached.isValid()).isTrue();

			verify(batchFactory, times(2)).get();
			verifyNoMoreInteractions(batch1);
			verify(batch2).close();
		}

		verifyNoMoreInteractions(batchFactory);
		verifyNoMoreInteractions(batch1);
		verifyNoMoreInteractions(batch2);
	}

	@Test
	public void invalidate() {
		Batch batch1 = mock(Batch.class);
		Batch batch2 = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = mock(Supplier.class);

		doReturn(batchFactory).when(manager).getBatchFactory();
		doReturn(batch1, batch2).when(batchFactory).get();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(detached::invalidate);

			verify(batchFactory).get();
			verify(batch1).close();
			verifyNoInteractions(batch2);

			Session<Object> session = mock(Session.class);

			doReturn(session).when(manager).findSession(this.id);

			detached.invalidate();

			verify(batchFactory, times(2)).get();
			verify(session).invalidate();
			verify(session).close();
			verifyNoMoreInteractions(batch1);
			verify(batch2).close();
		}

		verifyNoMoreInteractions(batchFactory);
		verifyNoMoreInteractions(batch1);
		verifyNoMoreInteractions(batch2);
	}

	@Test
	public void isExpired() {
		Batch batch1 = mock(Batch.class);
		Batch batch2 = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = mock(Supplier.class);

		doReturn(batchFactory).when(manager).getBatchFactory();
		doReturn(batch1, batch2).when(batchFactory).get();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(detached.getMetaData()::isExpired);

			verify(batchFactory).get();
			verify(batch1).close();
			verifyNoInteractions(batch2);

			Session<Object> session = mock(Session.class);
			SessionMetaData metaData = mock(SessionMetaData.class);
			boolean expected = this.random.nextBoolean();

			doReturn(session).when(manager).findSession(this.id);
			doReturn(metaData).when(session).getMetaData();
			doReturn(expected).when(metaData).isExpired();

			assertThat(detached.getMetaData().isExpired()).isEqualTo(expected);

			verify(batchFactory, times(2)).get();
			verify(session).close();
			verifyNoMoreInteractions(batch1);
			verify(batch2).close();
		}
	}

	@Test
	public void getCreationTime() {
		Batch batch = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = Supplier.of(batch);

		doReturn(batchFactory).when(manager).getBatchFactory();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(detached.getMetaData()::getCreationTime);

			verify(batch).close();
			reset(batch);

			Session<Object> session = mock(Session.class);
			SessionMetaData metaData = mock(SessionMetaData.class);
			Instant expected = Instant.now();

			doReturn(session).when(manager).findSession(this.id);
			doReturn(metaData).when(session).getMetaData();
			doReturn(expected).when(metaData).getCreationTime();

			assertThat(detached.getMetaData().getCreationTime()).isSameAs(expected);

			verify(batch).close();
		}
	}

	@Test
	public void getLastAccessStartTime() {
		Batch batch1 = mock(Batch.class);
		Batch batch2 = mock(Batch.class);
		Batch batch3 = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = mock(Supplier.class);

		doReturn(batchFactory).when(manager).getBatchFactory();
		doReturn(batch1, batch2, batch3).when(batchFactory).get();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(detached.getMetaData()::getLastAccessStartTime);

			verify(batchFactory).get();
			verify(batch1).close();
			verifyNoInteractions(batch2);
			verifyNoInteractions(batch3);

			Session<Object> session = mock(Session.class);
			SessionMetaData metaData = mock(SessionMetaData.class);
			Instant expected = Instant.now();

			doReturn(session).when(manager).findSession(this.id);
			doReturn(metaData).when(session).getMetaData();
			doReturn(Optional.empty(), Optional.of(expected)).when(metaData).getLastAccessStartTime();

			assertThat(detached.getMetaData().getLastAccessStartTime()).isEmpty();

			verify(batchFactory, times(2)).get();
			verifyNoMoreInteractions(batch1);
			verify(batch2).close();
			verifyNoInteractions(batch3);

			assertThat(detached.getMetaData().getLastAccessStartTime()).hasValue(expected);

			verify(batchFactory, times(3)).get();
			verifyNoMoreInteractions(batch1);
			verifyNoMoreInteractions(batch2);
			verify(batch3).close();
		}

		verifyNoMoreInteractions(batchFactory);
		verifyNoMoreInteractions(batch1);
		verifyNoMoreInteractions(batch2);
		verifyNoMoreInteractions(batch3);
	}

	@Test
	public void getLastAccessEndTime() {
		Batch batch1 = mock(Batch.class);
		Batch batch2 = mock(Batch.class);
		Batch batch3 = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = mock(Supplier.class);

		doReturn(batchFactory).when(manager).getBatchFactory();
		doReturn(batch1, batch2, batch3).when(batchFactory).get();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(detached.getMetaData()::getLastAccessEndTime);

			verify(batchFactory).get();
			verify(batch1).close();
			verifyNoInteractions(batch2);
			verifyNoInteractions(batch3);

			Session<Object> session = mock(Session.class);
			SessionMetaData metaData = mock(SessionMetaData.class);
			Instant expected = Instant.now();

			doReturn(session).when(manager).findSession(this.id);
			doReturn(metaData).when(session).getMetaData();
			doReturn(Optional.empty(), Optional.of(expected)).when(metaData).getLastAccessEndTime();

			assertThat(detached.getMetaData().getLastAccessEndTime()).isEmpty();

			verify(batchFactory, times(2)).get();
			verifyNoMoreInteractions(batch1);
			verify(batch2).close();
			verifyNoInteractions(batch3);

			assertThat(detached.getMetaData().getLastAccessEndTime()).hasValue(expected);

			verify(batchFactory, times(3)).get();
			verifyNoMoreInteractions(batch1);
			verifyNoMoreInteractions(batch2);
			verify(batch3).close();
		}

		verifyNoMoreInteractions(batchFactory);
		verifyNoMoreInteractions(batch1);
		verifyNoMoreInteractions(batch2);
		verifyNoMoreInteractions(batch3);
	}

	@Test
	public void getMaxIdle() {
		Batch batch1 = mock(Batch.class);
		Batch batch2 = mock(Batch.class);
		Batch batch3 = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = mock(Supplier.class);

		doReturn(batchFactory).when(manager).getBatchFactory();
		doReturn(batch1, batch2, batch3).when(batchFactory).get();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(detached.getMetaData()::getMaxIdle);

			verify(batchFactory).get();
			verify(batch1).close();
			verifyNoInteractions(batch2);
			verifyNoInteractions(batch3);

			Session<Object> session = mock(Session.class);
			SessionMetaData metaData = mock(SessionMetaData.class);
			Duration expected = Duration.ZERO;

			doReturn(session).when(manager).findSession(this.id);
			doReturn(metaData).when(session).getMetaData();
			doReturn(Optional.empty(), Optional.of(expected)).when(metaData).getMaxIdle();

			assertThat(detached.getMetaData().getMaxIdle()).isEmpty();

			verify(batchFactory, times(2)).get();
			verifyNoMoreInteractions(batch1);
			verify(batch2).close();
			verifyNoInteractions(batch3);

			assertThat(detached.getMetaData().getMaxIdle()).hasValue(expected);

			verify(batchFactory, times(3)).get();
			verifyNoMoreInteractions(batch1);
			verifyNoMoreInteractions(batch2);
			verify(batch3).close();
		}

		verifyNoMoreInteractions(batchFactory);
		verifyNoMoreInteractions(batch1);
		verifyNoMoreInteractions(batch2);
		verifyNoMoreInteractions(batch3);
	}

	@Test
	public void setLastAccess() {
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = mock(Supplier.class);

		doReturn(batchFactory).when(manager).getBatchFactory();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> detached.getMetaData().setLastAccess(Instant.now(), Instant.now()));
		}

		verifyNoInteractions(batchFactory);
	}

	@Test
	public void setMaxIdle() {
		Duration duration = Duration.ofMinutes(this.random.nextInt(Byte.MAX_VALUE) + 1);
		Batch batch1 = mock(Batch.class);
		Batch batch2 = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = mock(Supplier.class);

		doReturn(batchFactory).when(manager).getBatchFactory();
		doReturn(batch1, batch2).when(batchFactory).get();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> detached.getMetaData().setMaxIdle(duration));

			verify(batchFactory).get();
			verify(batch1).close();
			verifyNoInteractions(batch2);

			Session<Object> session = mock(Session.class);
			SessionMetaData metaData = mock(SessionMetaData.class);

			doReturn(session).when(manager).findSession(this.id);
			doReturn(metaData).when(session).getMetaData();

			detached.getMetaData().setMaxIdle(duration);

			verify(metaData).setMaxIdle(duration);
			verify(session).close();
			verify(batchFactory, times(2)).get();
			verifyNoMoreInteractions(batch1);
			verify(batch2).close();
		}

		verifyNoMoreInteractions(batchFactory);
		verifyNoMoreInteractions(batch1);
		verifyNoMoreInteractions(batch2);
	}

	@Test
	public void getAttributeNames() {
		Batch batch1 = mock(Batch.class);
		Batch batch2 = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = mock(Supplier.class);

		doReturn(batchFactory).when(manager).getBatchFactory();
		doReturn(batch1, batch2).when(batchFactory).get();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(detached.getAttributes()::keySet);

			verify(batchFactory).get();
			verify(batch1).close();
			verifyNoInteractions(batch2);

			Session<Object> session = mock(Session.class);
			Map<String, Object> attributes = mock(Map.class);
			Set<String> expected = Collections.singleton("foo");

			doReturn(session).when(manager).findSession(this.id);
			doReturn(attributes).when(session).getAttributes();
			doReturn(expected).when(attributes).keySet();

			assertThat(detached.getAttributes().keySet()).isSameAs(expected);

			verify(batchFactory, times(2)).get();
			verifyNoMoreInteractions(batch1);
			verify(batch2).close();
		}

		verifyNoMoreInteractions(batchFactory);
		verifyNoMoreInteractions(batch1);
		verifyNoMoreInteractions(batch2);
	}

	@Test
	public void getAttribute() {
		Batch batch1 = mock(Batch.class);
		Batch batch2 = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = mock(Supplier.class);

		doReturn(batchFactory).when(manager).getBatchFactory();
		doReturn(batch1, batch2).when(batchFactory).get();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			String attributeName = "foo";

			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> detached.getAttributes().get(attributeName));

			verify(batchFactory).get();
			verify(batch1).close();
			verifyNoInteractions(batch2);

			Session<Object> session = mock(Session.class);
			Map<String, Object> attributes = mock(Map.class);
			Object expected = new Object();

			doReturn(session).when(manager).findSession(this.id);
			doReturn(attributes).when(session).getAttributes();
			doReturn(expected).when(attributes).get(attributeName);

			assertThat(detached.getAttributes().get(attributeName)).isSameAs(expected);

			verify(batchFactory, times(2)).get();
			verifyNoMoreInteractions(batch1);
			verify(batch2).close();
		}

		verifyNoMoreInteractions(batchFactory);
		verifyNoMoreInteractions(batch1);
		verifyNoMoreInteractions(batch2);
	}

	@Test
	public void setAttribute() {
		Batch batch1 = mock(Batch.class);
		Batch batch2 = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = mock(Supplier.class);

		doReturn(batchFactory).when(manager).getBatchFactory();
		doReturn(batch1, batch2).when(batchFactory).get();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			String attributeName = "foo";
			Object attributeValue = "bar";

			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> detached.getAttributes().put(attributeName, attributeValue));

			verify(batchFactory).get();
			verify(batch1).close();
			verifyNoInteractions(batch2);

			Session<Object> session = mock(Session.class);
			Map<String, Object> attributes = mock(Map.class);
			Object expected = new Object();

			doReturn(session).when(manager).findSession(this.id);
			doReturn(attributes).when(session).getAttributes();
			doReturn(expected).when(attributes).put(attributeName, attributeValue);

			assertThat(detached.getAttributes().put(attributeName, attributeValue)).isSameAs(expected);

			verify(session).close();

			verify(batchFactory, times(2)).get();
			verifyNoMoreInteractions(batch1);
			verify(batch2).close();
		}

		verifyNoMoreInteractions(batchFactory);
		verifyNoMoreInteractions(batch1);
		verifyNoMoreInteractions(batch2);
	}

	@Test
	public void removeAttribute() {
		Batch batch1 = mock(Batch.class);
		Batch batch2 = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = mock(Supplier.class);

		doReturn(batchFactory).when(manager).getBatchFactory();
		doReturn(batch1, batch2).when(batchFactory).get();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			String attributeName = "foo";

			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> detached.getAttributes().remove(attributeName));

			verify(batchFactory).get();
			verify(batch1).close();
			verifyNoInteractions(batch2);

			Session<Object> session = mock(Session.class);
			Map<String, Object> attributes = mock(Map.class);
			Object expected = new Object();

			doReturn(session).when(manager).findSession(this.id);
			doReturn(attributes).when(session).getAttributes();
			doReturn(expected).when(attributes).remove(attributeName);

			assertThat(detached.getAttributes().remove(attributeName)).isSameAs(expected);

			verify(batchFactory, times(2)).get();
			verifyNoMoreInteractions(batch1);
			verify(batch2).close();
		}

		verifyNoMoreInteractions(batchFactory);
		verifyNoMoreInteractions(batch1);
		verifyNoMoreInteractions(batch2);
	}
}
