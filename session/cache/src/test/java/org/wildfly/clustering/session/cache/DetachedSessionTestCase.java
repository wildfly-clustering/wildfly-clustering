/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.batch.Batcher;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionMetaData;

/**
 * Unit test for {@link DetachedSession}.
 * @author Paul Ferraro
 */
public class DetachedSessionTestCase {
	private final SessionManager<Object, Batch> manager = mock(SessionManager.class);
	private final String id = UUID.randomUUID().toString();
	private final Object localContext = new Object();

	private final Session<Object> session = new DetachedSession<>(this.manager, this.id, this.localContext);

	@Test
	public void getId() {
		assertSame(this.id, this.session.getId());
	}

	@Test
	public void isNew() {
		assertFalse(this.session.getMetaData().isNew());
	}

	@Test
	public void isValid() {
		Batcher<Batch> batcher = mock(Batcher.class);
		Batch batch = mock(Batch.class);

		when(this.manager.getBatcher()).thenReturn(batcher);
		when(batcher.createBatch()).thenReturn(batch);
		when(this.manager.findImmutableSession(this.id)).thenReturn(null);

		assertFalse(this.session.isValid());

		verify(batch).close();
		reset(batch);

		ImmutableSession session = mock(ImmutableSession.class);

		when(this.manager.findImmutableSession(this.id)).thenReturn(session);

		assertTrue(this.session.isValid());

		verify(batch).close();
	}

	@Test
	public void invalidate() {
		Batcher<Batch> batcher = mock(Batcher.class);
		Batch batch = mock(Batch.class);

		when(this.manager.getBatcher()).thenReturn(batcher);
		when(batcher.createBatch()).thenReturn(batch);
		when(this.manager.findSession(this.id)).thenReturn(null);

		assertThrows(IllegalStateException.class, this.session::invalidate);

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);

		when(this.manager.findSession(this.id)).thenReturn(session);

		this.session.invalidate();

		verify(session).invalidate();
		verify(session).close();
		verify(batch).close();
	}

	@Test
	public void isExpired() {
		Batcher<Batch> batcher = mock(Batcher.class);
		Batch batch = mock(Batch.class);

		when(this.manager.getBatcher()).thenReturn(batcher);
		when(batcher.createBatch()).thenReturn(batch);
		when(this.manager.findSession(this.id)).thenReturn(null);

		assertThrows(IllegalStateException.class, this.session.getMetaData()::isExpired);

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		SessionMetaData metaData = mock(SessionMetaData.class);
		boolean expected = true;

		when(this.manager.findSession(this.id)).thenReturn(session);
		when(session.getMetaData()).thenReturn(metaData);
		when(metaData.isExpired()).thenReturn(expected);

		boolean result = this.session.getMetaData().isExpired();

		assertEquals(expected, result);

		verify(batch).close();
	}

	@Test
	public void getCreationTime() {
		Batcher<Batch> batcher = mock(Batcher.class);
		Batch batch = mock(Batch.class);

		when(this.manager.getBatcher()).thenReturn(batcher);
		when(batcher.createBatch()).thenReturn(batch);
		when(this.manager.findSession(this.id)).thenReturn(null);

		assertThrows(IllegalStateException.class, this.session.getMetaData()::getCreationTime);

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		SessionMetaData metaData = mock(SessionMetaData.class);
		Instant expected = Instant.now();

		when(this.manager.findSession(this.id)).thenReturn(session);
		when(session.getMetaData()).thenReturn(metaData);
		when(metaData.getCreationTime()).thenReturn(expected);

		Instant result = this.session.getMetaData().getCreationTime();

		assertSame(expected, result);

		verify(batch).close();
	}

	@Test
	public void getLastAccessStartTime() {
		Batcher<Batch> batcher = mock(Batcher.class);
		Batch batch = mock(Batch.class);

		when(this.manager.getBatcher()).thenReturn(batcher);
		when(batcher.createBatch()).thenReturn(batch);
		when(this.manager.findSession(this.id)).thenReturn(null);

		assertThrows(IllegalStateException.class, this.session.getMetaData()::getLastAccessStartTime);

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		SessionMetaData metaData = mock(SessionMetaData.class);
		Instant expected = Instant.now();

		when(this.manager.findSession(this.id)).thenReturn(session);
		when(session.getMetaData()).thenReturn(metaData);
		when(metaData.getLastAccessStartTime()).thenReturn(expected);

		Instant result = this.session.getMetaData().getLastAccessStartTime();

		assertSame(expected, result);

		verify(batch).close();
	}

	@Test
	public void getLastAccessEndTime() {
		Batcher<Batch> batcher = mock(Batcher.class);
		Batch batch = mock(Batch.class);

		when(this.manager.getBatcher()).thenReturn(batcher);
		when(batcher.createBatch()).thenReturn(batch);
		when(this.manager.findSession(this.id)).thenReturn(null);

		assertThrows(IllegalStateException.class, this.session.getMetaData()::getLastAccessEndTime);

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		SessionMetaData metaData = mock(SessionMetaData.class);
		Instant expected = Instant.now();

		when(this.manager.findSession(this.id)).thenReturn(session);
		when(session.getMetaData()).thenReturn(metaData);
		when(metaData.getLastAccessEndTime()).thenReturn(expected);

		Instant result = this.session.getMetaData().getLastAccessEndTime();

		assertSame(expected, result);

		verify(batch).close();
	}

	@Test
	public void getMaxInactiveInterval() {
		Batcher<Batch> batcher = mock(Batcher.class);
		Batch batch = mock(Batch.class);

		when(this.manager.getBatcher()).thenReturn(batcher);
		when(batcher.createBatch()).thenReturn(batch);
		when(this.manager.findSession(this.id)).thenReturn(null);

		assertThrows(IllegalStateException.class, this.session.getMetaData()::getTimeout);

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		SessionMetaData metaData = mock(SessionMetaData.class);
		Duration expected = Duration.ZERO;

		when(this.manager.findSession(this.id)).thenReturn(session);
		when(session.getMetaData()).thenReturn(metaData);
		when(metaData.getTimeout()).thenReturn(expected);

		Duration result = this.session.getMetaData().getTimeout();

		assertSame(expected, result);

		verify(batch).close();
	}

	@Test
	public void setLastAccess() {
		assertThrows(IllegalStateException.class, () -> this.session.getMetaData().setLastAccess(Instant.now(), Instant.now()));
	}

	@Test
	public void setTimeout() {
		Batcher<Batch> batcher = mock(Batcher.class);
		Batch batch = mock(Batch.class);
		Duration duration = Duration.ZERO;

		when(this.manager.getBatcher()).thenReturn(batcher);
		when(batcher.createBatch()).thenReturn(batch);
		when(this.manager.findSession(this.id)).thenReturn(null);

		assertThrows(IllegalStateException.class, () -> this.session.getMetaData().setTimeout(duration));

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		SessionMetaData metaData = mock(SessionMetaData.class);

		when(this.manager.findSession(this.id)).thenReturn(session);
		when(session.getMetaData()).thenReturn(metaData);

		this.session.getMetaData().setTimeout(duration);

		verify(metaData).setTimeout(duration);
		verify(session).close();
		verify(batch).close();
	}

	@Test
	public void getAttributeNames() {
		Batcher<Batch> batcher = mock(Batcher.class);
		Batch batch = mock(Batch.class);

		when(this.manager.getBatcher()).thenReturn(batcher);
		when(batcher.createBatch()).thenReturn(batch);
		when(this.manager.findSession(this.id)).thenReturn(null);

		assertThrows(IllegalStateException.class, this.session.getAttributes()::keySet);

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		Map<String, Object> attributes = mock(Map.class);
		Set<String> expected = Collections.singleton("foo");

		when(this.manager.findSession(this.id)).thenReturn(session);
		when(session.getAttributes()).thenReturn(attributes);
		when(attributes.keySet()).thenReturn(expected);

		Set<String> result = this.session.getAttributes().keySet();

		assertSame(expected, result);

		verify(batch).close();
	}

	@Test
	public void getAttribute() {
		Batcher<Batch> batcher = mock(Batcher.class);
		Batch batch = mock(Batch.class);
		String attributeName = "foo";

		when(this.manager.getBatcher()).thenReturn(batcher);
		when(batcher.createBatch()).thenReturn(batch);
		when(this.manager.findSession(this.id)).thenReturn(null);

		assertThrows(IllegalStateException.class, () -> this.session.getAttributes().get(attributeName));

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		Map<String, Object> attributes = mock(Map.class);
		Object expected = new Object();

		when(this.manager.findSession(this.id)).thenReturn(session);
		when(session.getAttributes()).thenReturn(attributes);
		when(attributes.get(attributeName)).thenReturn(expected);

		Object result = this.session.getAttributes().get(attributeName);

		assertSame(expected, result);

		verify(batch).close();
	}

	@Test
	public void setAttribute() {
		Batcher<Batch> batcher = mock(Batcher.class);
		Batch batch = mock(Batch.class);
		String attributeName = "foo";
		Object attributeValue = "bar";

		when(this.manager.getBatcher()).thenReturn(batcher);
		when(batcher.createBatch()).thenReturn(batch);
		when(this.manager.findSession(this.id)).thenReturn(null);

		assertThrows(IllegalStateException.class, () -> this.session.getAttributes().put(attributeName, attributeValue));

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		Map<String, Object> attributes = mock(Map.class);
		Object expected = new Object();

		when(this.manager.findSession(this.id)).thenReturn(session);
		when(session.getAttributes()).thenReturn(attributes);
		when(attributes.put(attributeName, attributeValue)).thenReturn(expected);

		Object result = this.session.getAttributes().put(attributeName, attributeValue);

		assertSame(expected, result);

		verify(session).close();
		verify(batch).close();
	}

	@Test
	public void removeAttribute() {
		Batcher<Batch> batcher = mock(Batcher.class);
		Batch batch = mock(Batch.class);
		String attributeName = "foo";

		when(this.manager.getBatcher()).thenReturn(batcher);
		when(batcher.createBatch()).thenReturn(batch);
		when(this.manager.findSession(this.id)).thenReturn(null);

		assertThrows(IllegalStateException.class, () -> this.session.getAttributes().remove(attributeName));

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		Map<String, Object> attributes = mock(Map.class);
		Object expected = new Object();

		when(this.manager.findSession(this.id)).thenReturn(session);
		when(session.getAttributes()).thenReturn(attributes);
		when(attributes.remove(attributeName)).thenReturn(expected);

		Object result = this.session.getAttributes().remove(attributeName);

		assertSame(expected, result);

		verify(session).close();
		verify(batch).close();
	}
}
