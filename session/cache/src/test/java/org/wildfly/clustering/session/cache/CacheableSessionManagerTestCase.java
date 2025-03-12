/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.server.cache.CacheStrategy;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionMetaData;
import org.wildfly.clustering.session.SessionStatistics;
import org.wildfly.clustering.session.cache.attributes.SessionAttributes;

/**
 * Unit test for {@link CachedSessionManager}.
 * @author Paul Ferraro
 */
public class CacheableSessionManagerTestCase {

	@SuppressWarnings("unchecked")
	@Test
	public void findSession() {
		SessionManager<Void> manager = mock(SessionManager.class);
		SessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.CONCURRENT);
		Session<Void> expected1 = mock(Session.class);
		Session<Void> expected2 = mock(Session.class);
		String id = "foo";
		SessionMetaData metaData1 = mock(SessionMetaData.class);
		SessionAttributes attributes1 = mock(SessionAttributes.class);
		SessionMetaData metaData2 = mock(SessionMetaData.class);
		SessionAttributes attributes2 = mock(SessionAttributes.class);

		when(manager.findSessionAsync(id)).thenReturn(CompletableFuture.completedStage(expected1), CompletableFuture.completedStage(expected2));
		when(expected1.getId()).thenReturn(id);
		when(expected1.isValid()).thenReturn(true);
		when(expected1.getAttributes()).thenReturn(attributes1);
		when(expected1.getMetaData()).thenReturn(metaData1);
		when(expected2.getId()).thenReturn(id);
		when(expected2.isValid()).thenReturn(true);
		when(expected2.getAttributes()).thenReturn(attributes2);
		when(expected2.getMetaData()).thenReturn(metaData2);

		try (Session<Void> session1 = subject.findSession(id)) {
			assertThat(session1).isNotNull();
			assertThat(session1.getId()).isSameAs(id);
			assertThat(session1.getMetaData()).isSameAs(metaData1);
			assertThat(session1.getAttributes()).isSameAs(attributes1);

			try (Session<Void> session2 = subject.findSession(id)) {
				assertThat(session2).isNotNull();
				// Should return the same session without invoking the manager
				assertThat(session2).isSameAs(session1);
			}

			// Should not trigger Session.close() yet
			verify(expected1, never()).close();
		}

		verify(expected1).close();

		// Should use second session instance
		try (Session<Void> session = subject.findSession(id)) {
			assertThat(session).isNotNull();
			assertThat(session.getId()).isSameAs(id);
			assertThat(session.getMetaData()).isSameAs(metaData2);
			assertThat(session.getAttributes()).isSameAs(attributes2);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void findInvalidSession() {
		SessionManager<Void> manager = mock(SessionManager.class);
		SessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.CONCURRENT);
		Session<Void> expected1 = mock(Session.class);
		String id = "foo";
		SessionMetaData metaData1 = mock(SessionMetaData.class);
		SessionAttributes attributes1 = mock(SessionAttributes.class);

		when(manager.findSessionAsync(id)).thenReturn(CompletableFuture.completedStage(expected1), CompletableFuture.completedStage(null));
		when(expected1.getId()).thenReturn(id);
		when(expected1.isValid()).thenReturn(true);
		when(expected1.getAttributes()).thenReturn(attributes1);
		when(expected1.getMetaData()).thenReturn(metaData1);

		try (Session<Void> session1 = subject.findSession(id)) {
			assertThat(session1).isNotNull();
			assertThat(session1.getId()).isSameAs(id);
			assertThat(session1.getMetaData()).isSameAs(metaData1);
			assertThat(session1.getAttributes()).isSameAs(attributes1);

			session1.invalidate();

			verify(expected1).invalidate();
		}

		verify(expected1).close();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void createSession() {
		SessionManager<Void> manager = mock(SessionManager.class);
		SessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.CONCURRENT);
		Session<Void> expected1 = mock(Session.class);
		Session<Void> expected2 = mock(Session.class);
		String id = "foo";
		SessionMetaData metaData1 = mock(SessionMetaData.class);
		SessionAttributes attributes1 = mock(SessionAttributes.class);
		SessionMetaData metaData2 = mock(SessionMetaData.class);
		SessionAttributes attributes2 = mock(SessionAttributes.class);

		when(manager.createSessionAsync(id)).thenReturn(CompletableFuture.completedStage(expected1), CompletableFuture.completedStage(expected2));
		when(expected1.getId()).thenReturn(id);
		when(expected1.isValid()).thenReturn(true);
		when(expected1.getAttributes()).thenReturn(attributes1);
		when(expected1.getMetaData()).thenReturn(metaData1);
		when(expected2.getId()).thenReturn(id);
		when(expected2.isValid()).thenReturn(true);
		when(expected2.getAttributes()).thenReturn(attributes2);
		when(expected2.getMetaData()).thenReturn(metaData2);

		try (Session<Void> session1 = subject.createSession(id)) {
			assertThat(session1).isNotNull();
			assertThat(session1.getId()).isSameAs(id);
			assertThat(session1.getMetaData()).isSameAs(metaData1);
			assertThat(session1.getAttributes()).isSameAs(attributes1);

			try (Session<Void> session2 = subject.findSession(id)) {
				assertThat(session2).isNotNull();
				// Should return the same session without invoking the manager
				assertThat(session2).isSameAs(session1);
			}

			// Should not trigger Session.close() yet
			verify(expected1, never()).close();
		}

		verify(expected1).close();

		// Should use second session instance
		try (Session<Void> session = subject.createSession(id)) {
			assertThat(session).isNotNull();
			assertThat(session.getId()).isSameAs(id);
			assertThat(session.getMetaData()).isSameAs(metaData2);
			assertThat(session.getAttributes()).isSameAs(attributes2);
		}
	}

	@Test
	public void getIdentifierFactory() {
		SessionManager<Void> manager = mock(SessionManager.class);
		SessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.NONE);
		Supplier<String> expected = mock(Supplier.class);

		when(manager.getIdentifierFactory()).thenReturn(expected);

		Supplier<String> result = subject.getIdentifierFactory();

		assertThat(result).isSameAs(expected);
	}

	@Test
	public void start() {
		SessionManager<Void> manager = mock(SessionManager.class);
		SessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.NONE);

		subject.start();

		verify(manager).start();
	}

	@Test
	public void stop() {
		SessionManager<Void> manager = mock(SessionManager.class);
		SessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.NONE);

		subject.stop();

		verify(manager).stop();
	}

	@Test
	public void getStatistics() {
		SessionManager<Void> manager = mock(SessionManager.class);
		SessionStatistics statistics = mock(SessionStatistics.class);
		SessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.NONE);

		when(manager.getStatistics()).thenReturn(statistics);

		assertThat(subject.getStatistics()).isSameAs(statistics);
	}

	@Test
	public void getBatcher() {
		SessionManager<Void> manager = mock(SessionManager.class);
		SessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.NONE);
		Supplier<Batch> expected = mock(Supplier.class);

		when(manager.getBatchFactory()).thenReturn(expected);

		Supplier<Batch> result = subject.getBatchFactory();

		assertThat(result).isSameAs(expected);
	}

	@Test
	public void findImmutableSession() {
		SessionManager<Void> manager = mock(SessionManager.class);
		SessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.NONE);
		ImmutableSession expected = mock(ImmutableSession.class);
		String id = "foo";

		when(manager.findImmutableSessionAsync(id)).thenReturn(CompletableFuture.completedStage(expected));

		ImmutableSession result = subject.findImmutableSession(id);

		assertThat(result).isSameAs(expected);
	}
}
