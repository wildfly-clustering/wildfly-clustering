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
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionMetaData;

/**
 * Unit test for {@link DetachedSession}.
 * @author Paul Ferraro
 */
public class DetachedSessionTestCase {
	private final SessionManager<Object> manager = mock(SessionManager.class);
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
		Supplier<Batch> batchFactory = mock(Supplier.class);
		Batch batch = mock(Batch.class);

		doReturn(batchFactory).when(this.manager).getBatchFactory();
		doReturn(batch).when(batchFactory).get();
		doReturn(null).when(this.manager).findImmutableSession(this.id);

		assertFalse(this.session.isValid());

		verify(batch).close();
		reset(batch);

		ImmutableSession session = mock(ImmutableSession.class);

		doReturn(session).when(this.manager).findImmutableSession(this.id);

		assertTrue(this.session.isValid());

		verify(batch).close();
	}

	@Test
	public void invalidate() {
		Supplier<Batch> batchFactory = mock(Supplier.class);
		Batch batch = mock(Batch.class);

		doReturn(batchFactory).when(this.manager).getBatchFactory();
		doReturn(batch).when(batchFactory).get();
		doReturn(null).when(this.manager).findSession(this.id);

		assertThrows(IllegalStateException.class, this.session::invalidate);

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);

		doReturn(session).when(this.manager).findSession(this.id);

		this.session.invalidate();

		verify(session).invalidate();
		verify(session).close();
		verify(batch).close();
	}

	@Test
	public void isExpired() {
		Supplier<Batch> batchFactory = mock(Supplier.class);
		Batch batch = mock(Batch.class);

		doReturn(batchFactory).when(this.manager).getBatchFactory();
		doReturn(batch).when(batchFactory).get();
		doReturn(null).when(this.manager).findSession(this.id);

		assertThrows(IllegalStateException.class, this.session.getMetaData()::isExpired);

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		SessionMetaData metaData = mock(SessionMetaData.class);
		boolean expected = true;

		doReturn(session).when(this.manager).findSession(this.id);
		doReturn(metaData).when(session).getMetaData();
		doReturn(expected).when(metaData).isExpired();

		boolean result = this.session.getMetaData().isExpired();

		assertEquals(expected, result);

		verify(batch).close();
	}

	@Test
	public void getCreationTime() {
		Supplier<Batch> batchFactory = mock(Supplier.class);
		Batch batch = mock(Batch.class);

		doReturn(batchFactory).when(this.manager).getBatchFactory();
		doReturn(batch).when(batchFactory).get();
		doReturn(null).when(this.manager).findSession(this.id);

		assertThrows(IllegalStateException.class, this.session.getMetaData()::getCreationTime);

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		SessionMetaData metaData = mock(SessionMetaData.class);
		Instant expected = Instant.now();

		doReturn(session).when(this.manager).findSession(this.id);
		doReturn(metaData).when(session).getMetaData();
		doReturn(expected).when(metaData).getCreationTime();

		Instant result = this.session.getMetaData().getCreationTime();

		assertSame(expected, result);

		verify(batch).close();
	}

	@Test
	public void getLastAccessStartTime() {
		Supplier<Batch> batchFactory = mock(Supplier.class);
		Batch batch = mock(Batch.class);

		doReturn(batchFactory).when(this.manager).getBatchFactory();
		doReturn(batch).when(batchFactory).get();
		doReturn(null).when(this.manager).findSession(this.id);

		assertThrows(IllegalStateException.class, this.session.getMetaData()::getLastAccessStartTime);

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		SessionMetaData metaData = mock(SessionMetaData.class);
		Instant expected = Instant.now();

		doReturn(session).when(this.manager).findSession(this.id);
		doReturn(metaData).when(session).getMetaData();
		doReturn(expected).when(metaData).getLastAccessStartTime();

		Instant result = this.session.getMetaData().getLastAccessStartTime();

		assertSame(expected, result);

		verify(batch).close();
	}

	@Test
	public void getLastAccessEndTime() {
		Supplier<Batch> batchFactory = mock(Supplier.class);
		Batch batch = mock(Batch.class);

		doReturn(batchFactory).when(this.manager).getBatchFactory();
		doReturn(batch).when(batchFactory).get();
		doReturn(null).when(this.manager).findSession(this.id);

		assertThrows(IllegalStateException.class, this.session.getMetaData()::getLastAccessEndTime);

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		SessionMetaData metaData = mock(SessionMetaData.class);
		Instant expected = Instant.now();

		doReturn(session).when(this.manager).findSession(this.id);
		doReturn(metaData).when(session).getMetaData();
		doReturn(expected).when(metaData).getLastAccessEndTime();

		Instant result = this.session.getMetaData().getLastAccessEndTime();

		assertSame(expected, result);

		verify(batch).close();
	}

	@Test
	public void getMaxInactiveInterval() {
		Supplier<Batch> batchFactory = mock(Supplier.class);
		Batch batch = mock(Batch.class);

		doReturn(batchFactory).when(this.manager).getBatchFactory();
		doReturn(batch).when(batchFactory).get();
		doReturn(null).when(this.manager).findSession(this.id);

		assertThrows(IllegalStateException.class, this.session.getMetaData()::getTimeout);

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		SessionMetaData metaData = mock(SessionMetaData.class);
		Duration expected = Duration.ZERO;

		doReturn(session).when(this.manager).findSession(this.id);
		doReturn(metaData).when(session).getMetaData();
		doReturn(expected).when(metaData).getTimeout();

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
		Supplier<Batch> batchFactory = mock(Supplier.class);
		Batch batch = mock(Batch.class);
		Duration duration = Duration.ZERO;

		doReturn(batchFactory).when(this.manager).getBatchFactory();
		doReturn(batch).when(batchFactory).get();
		doReturn(null).when(this.manager).findSession(this.id);

		assertThrows(IllegalStateException.class, () -> this.session.getMetaData().setTimeout(duration));

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		SessionMetaData metaData = mock(SessionMetaData.class);

		doReturn(session).when(this.manager).findSession(this.id);
		doReturn(metaData).when(session).getMetaData();

		this.session.getMetaData().setTimeout(duration);

		verify(metaData).setTimeout(duration);
		verify(session).close();
		verify(batch).close();
	}

	@Test
	public void getAttributeNames() {
		Supplier<Batch> batchFactory = mock(Supplier.class);
		Batch batch = mock(Batch.class);

		doReturn(batchFactory).when(this.manager).getBatchFactory();
		doReturn(batch).when(batchFactory).get();
		doReturn(null).when(this.manager).findSession(this.id);

		assertThrows(IllegalStateException.class, this.session.getAttributes()::keySet);

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		Map<String, Object> attributes = mock(Map.class);
		Set<String> expected = Collections.singleton("foo");

		doReturn(session).when(this.manager).findSession(this.id);
		doReturn(attributes).when(session).getAttributes();
		doReturn(expected).when(attributes).keySet();

		Set<String> result = this.session.getAttributes().keySet();

		assertSame(expected, result);

		verify(batch).close();
	}

	@Test
	public void getAttribute() {
		Supplier<Batch> batchFactory = mock(Supplier.class);
		Batch batch = mock(Batch.class);
		String attributeName = "foo";

		doReturn(batchFactory).when(this.manager).getBatchFactory();
		doReturn(batch).when(batchFactory).get();
		doReturn(null).when(this.manager).findSession(this.id);

		assertThrows(IllegalStateException.class, () -> this.session.getAttributes().get(attributeName));

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		Map<String, Object> attributes = mock(Map.class);
		Object expected = new Object();

		doReturn(session).when(this.manager).findSession(this.id);
		doReturn(attributes).when(session).getAttributes();
		doReturn(expected).when(attributes).get(attributeName);

		Object result = this.session.getAttributes().get(attributeName);

		assertSame(expected, result);

		verify(batch).close();
	}

	@Test
	public void setAttribute() {
		Supplier<Batch> batchFactory = mock(Supplier.class);
		Batch batch = mock(Batch.class);
		String attributeName = "foo";
		Object attributeValue = "bar";

		doReturn(batchFactory).when(this.manager).getBatchFactory();
		doReturn(batch).when(batchFactory).get();
		doReturn(null).when(this.manager).findSession(this.id);

		assertThrows(IllegalStateException.class, () -> this.session.getAttributes().put(attributeName, attributeValue));

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		Map<String, Object> attributes = mock(Map.class);
		Object expected = new Object();

		doReturn(session).when(this.manager).findSession(this.id);
		doReturn(attributes).when(session).getAttributes();
		doReturn(expected).when(attributes).put(attributeName, attributeValue);

		Object result = this.session.getAttributes().put(attributeName, attributeValue);

		assertSame(expected, result);

		verify(session).close();
		verify(batch).close();
	}

	@Test
	public void removeAttribute() {
		Supplier<Batch> batchFactory = mock(Supplier.class);
		Batch batch = mock(Batch.class);
		String attributeName = "foo";

		doReturn(batchFactory).when(this.manager).getBatchFactory();
		doReturn(batch).when(batchFactory).get();
		doReturn(null).when(this.manager).findSession(this.id);

		assertThrows(IllegalStateException.class, () -> this.session.getAttributes().remove(attributeName));

		verify(batch).close();
		reset(batch);

		Session<Object> session = mock(Session.class);
		Map<String, Object> attributes = mock(Map.class);
		Object expected = new Object();

		doReturn(session).when(this.manager).findSession(this.id);
		doReturn(attributes).when(session).getAttributes();
		doReturn(expected).when(attributes).remove(attributeName);

		Object result = this.session.getAttributes().remove(attributeName);

		assertSame(expected, result);

		verify(session).close();
		verify(batch).close();
	}
}
