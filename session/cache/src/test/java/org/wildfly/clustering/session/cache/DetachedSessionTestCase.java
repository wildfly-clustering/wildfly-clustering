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
		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			assertThat(detached.getId()).isSameAs(this.id);
		}
	}

	@Test
	public void isNew() {
		SessionManager<Object> manager = mock(SessionManager.class);
		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			assertThat(detached.getMetaData().isNew()).isFalse();
		}
	}

	@Test
	public void isValid() {
		Batch batch = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = Supplier.of(batch);

		doReturn(batchFactory).when(manager).getBatchFactory();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			doReturn(null).when(manager).findImmutableSession(this.id);

			assertThat(detached.isValid()).isFalse();

			verify(batch).close();
			reset(batch);

			ImmutableSession session = mock(ImmutableSession.class);

			doReturn(session).when(manager).findImmutableSession(this.id);

			assertThat(detached.isValid()).isTrue();

			verify(batch).close();
		}
	}

	@Test
	public void invalidate() {
		Batch batch = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = Supplier.of(batch);

		doReturn(batchFactory).when(manager).getBatchFactory();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(detached::invalidate);

			verify(batch).close();
			reset(batch);

			Session<Object> session = mock(Session.class);

			doReturn(session).when(manager).findSession(this.id);

			detached.invalidate();

			verify(session).invalidate();
			verify(session).close();
			verify(batch).close();
		}
	}

	@Test
	public void isExpired() {
		Batch batch = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = Supplier.of(batch);

		doReturn(batchFactory).when(manager).getBatchFactory();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(detached.getMetaData()::isExpired);

			verify(batch).close();
			reset(batch);

			Session<Object> session = mock(Session.class);
			SessionMetaData metaData = mock(SessionMetaData.class);
			boolean expected = this.random.nextBoolean();

			doReturn(session).when(manager).findSession(this.id);
			doReturn(metaData).when(session).getMetaData();
			doReturn(expected).when(metaData).isExpired();

			boolean result = detached.getMetaData().isExpired();

			assertThat(result).isEqualTo(expected);

			verify(batch).close();
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

			Instant result = detached.getMetaData().getCreationTime();

			assertThat(result).isSameAs(expected);

			verify(batch).close();
		}
	}

	@Test
	public void getLastAccessStartTime() {
		Batch batch = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = Supplier.of(batch);

		doReturn(batchFactory).when(manager).getBatchFactory();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(detached.getMetaData()::getLastAccessStartTime);

			verify(batch).close();
			reset(batch);

			Session<Object> session = mock(Session.class);
			SessionMetaData metaData = mock(SessionMetaData.class);
			Instant expected = Instant.now();

			doReturn(session).when(manager).findSession(this.id);
			doReturn(metaData).when(session).getMetaData();
			doReturn(expected).when(metaData).getLastAccessStartTime();

			Instant result = detached.getMetaData().getLastAccessStartTime();

			assertThat(result).isSameAs(expected);

			verify(batch).close();
		}
	}

	@Test
	public void getLastAccessEndTime() {
		Batch batch = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = Supplier.of(batch);

		doReturn(batchFactory).when(manager).getBatchFactory();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(detached.getMetaData()::getLastAccessEndTime);

			verify(batch).close();
			reset(batch);

			Session<Object> session = mock(Session.class);
			SessionMetaData metaData = mock(SessionMetaData.class);
			Instant expected = Instant.now();

			doReturn(session).when(manager).findSession(this.id);
			doReturn(metaData).when(session).getMetaData();
			doReturn(expected).when(metaData).getLastAccessEndTime();

			Instant result = detached.getMetaData().getLastAccessEndTime();

			assertThat(result).isSameAs(expected);

			verify(batch).close();
		}
	}

	@Test
	public void getMaxInactiveInterval() {
		Batch batch = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = Supplier.of(batch);

		doReturn(batchFactory).when(manager).getBatchFactory();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(detached.getMetaData()::getTimeout);

			verify(batch).close();
			reset(batch);

			Session<Object> session = mock(Session.class);
			SessionMetaData metaData = mock(SessionMetaData.class);
			Duration expected = Duration.ZERO;

			doReturn(session).when(manager).findSession(this.id);
			doReturn(metaData).when(session).getMetaData();
			doReturn(expected).when(metaData).getTimeout();

			Duration result = detached.getMetaData().getTimeout();

			assertThat(result).isSameAs(expected);

			verify(batch).close();
		}
	}

	@Test
	public void setLastAccess() {
		SessionManager<Object> manager = mock(SessionManager.class);
		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> detached.getMetaData().setLastAccess(Instant.now(), Instant.now()));
		}
	}

	@Test
	public void setTimeout() {
		Duration duration = Duration.ofMinutes(this.random.nextInt(Byte.MAX_VALUE) + 1);
		Batch batch = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = Supplier.of(batch);

		doReturn(batchFactory).when(manager).getBatchFactory();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> detached.getMetaData().setTimeout(duration));

			verify(batch).close();
			reset(batch);

			Session<Object> session = mock(Session.class);
			SessionMetaData metaData = mock(SessionMetaData.class);

			doReturn(session).when(manager).findSession(this.id);
			doReturn(metaData).when(session).getMetaData();

			detached.getMetaData().setTimeout(duration);

			verify(metaData).setTimeout(duration);
			verify(session).close();
			verify(batch).close();
		}
	}

	@Test
	public void getAttributeNames() {
		Batch batch = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = Supplier.of(batch);

		doReturn(batchFactory).when(manager).getBatchFactory();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(detached.getAttributes()::keySet);

			verify(batch).close();
			reset(batch);

			Session<Object> session = mock(Session.class);
			Map<String, Object> attributes = mock(Map.class);
			Set<String> expected = Collections.singleton("foo");

			doReturn(session).when(manager).findSession(this.id);
			doReturn(attributes).when(session).getAttributes();
			doReturn(expected).when(attributes).keySet();

			Set<String> result = detached.getAttributes().keySet();

			assertThat(result).isSameAs(expected);

			verify(batch).close();
		}
	}

	@Test
	public void getAttribute() {
		Batch batch = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = Supplier.of(batch);

		doReturn(batchFactory).when(manager).getBatchFactory();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			String attributeName = "foo";

			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> detached.getAttributes().get(attributeName));

			verify(batch).close();
			reset(batch);

			Session<Object> session = mock(Session.class);
			Map<String, Object> attributes = mock(Map.class);
			Object expected = new Object();

			doReturn(session).when(manager).findSession(this.id);
			doReturn(attributes).when(session).getAttributes();
			doReturn(expected).when(attributes).get(attributeName);

			Object result = detached.getAttributes().get(attributeName);

			assertThat(result).isSameAs(expected);

			verify(batch).close();
		}
	}

	@Test
	public void setAttribute() {
		Batch batch = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = Supplier.of(batch);

		doReturn(batchFactory).when(manager).getBatchFactory();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			String attributeName = "foo";
			Object attributeValue = "bar";

			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> detached.getAttributes().put(attributeName, attributeValue));

			verify(batch).close();
			reset(batch);

			Session<Object> session = mock(Session.class);
			Map<String, Object> attributes = mock(Map.class);
			Object expected = new Object();

			doReturn(session).when(manager).findSession(this.id);
			doReturn(attributes).when(session).getAttributes();
			doReturn(expected).when(attributes).put(attributeName, attributeValue);

			Object result = detached.getAttributes().put(attributeName, attributeValue);

			assertThat(result).isSameAs(expected);

			verify(session).close();
			verify(batch).close();
		}
	}

	@Test
	public void removeAttribute() {
		Batch batch = mock(Batch.class);
		SessionManager<Object> manager = mock(SessionManager.class);
		Supplier<Batch> batchFactory = Supplier.of(batch);

		doReturn(batchFactory).when(manager).getBatchFactory();

		try (Session<Object> detached = new DetachedSession<>(manager, this.id, this.localContext)) {
			String attributeName = "foo";

			doReturn(null).when(manager).findSession(this.id);

			assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> detached.getAttributes().remove(attributeName));

			verify(batch).close();
			reset(batch);

			Session<Object> session = mock(Session.class);
			Map<String, Object> attributes = mock(Map.class);
			Object expected = new Object();

			doReturn(session).when(manager).findSession(this.id);
			doReturn(attributes).when(session).getAttributes();
			doReturn(expected).when(attributes).remove(attributeName);

			Object result = detached.getAttributes().remove(attributeName);

			assertThat(result).isSameAs(expected);

			verify(session).close();
			verify(batch).close();
		}
	}
}
