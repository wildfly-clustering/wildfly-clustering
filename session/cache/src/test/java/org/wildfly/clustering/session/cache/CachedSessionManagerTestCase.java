/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.function.Supplier;
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
public class CachedSessionManagerTestCase {

	@Test
	public void findSession() {
		SessionManager<Void> manager = mock(SessionManager.class);
		CachedSessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.CONCURRENT);
		Session<Void> expected1 = mock(Session.class);
		Session<Void> expected2 = mock(Session.class);
		String id = "foo";
		SessionMetaData metaData1 = mock(SessionMetaData.class);
		SessionAttributes attributes1 = mock(SessionAttributes.class);
		SessionMetaData metaData2 = mock(SessionMetaData.class);
		SessionAttributes attributes2 = mock(SessionAttributes.class);

		doReturn(CompletableFuture.completedStage(expected1), CompletableFuture.completedStage(expected2)).when(manager).findSessionAsync(id);

		doReturn(id).when(expected1).getId();
		doReturn(true).when(expected1).isValid();
		doReturn(attributes1).when(expected1).getAttributes();
		doReturn(metaData1).when(expected1).getMetaData();
		doReturn(id).when(expected2).getId();
		doReturn(true).when(expected2).isValid();
		doReturn(attributes2).when(expected2).getAttributes();
		doReturn(metaData2).when(expected2).getMetaData();

		try (Session<Void> session1 = subject.findSession(id)) {
			verify(manager).findSessionAsync(id);

			assertThat(session1).isNotNull();
			assertThat(session1.getId()).isSameAs(id);
			assertThat(session1.getMetaData()).isSameAs(metaData1);
			assertThat(session1.getAttributes()).isSameAs(attributes1);

			assertThat(subject.keySet()).containsExactly(id);

			try (Session<Void> session2 = subject.findSession(id)) {
				// Should return the same session without invoking the manager
				verifyNoMoreInteractions(manager);

				assertThat(session2).isNotNull();
				assertThat(session2).isSameAs(session1);

				assertThat(subject.keySet()).containsExactly(id);
			}

			// Should not trigger Session.close() yet
			verify(expected1, never()).close();

			assertThat(subject.keySet()).containsExactly(id);
		}

		verify(expected1).close();

		// Cache should now be empty
		assertThat(subject.keySet()).isEmpty();

		try (Session<Void> session = subject.findSession(id)) {
			// Should use second session instance
			verify(manager, times(2)).findSessionAsync(id);

			assertThat(session).isNotNull();
			assertThat(session.getId()).isSameAs(id);
			assertThat(session.getMetaData()).isSameAs(metaData2);
			assertThat(session.getAttributes()).isSameAs(attributes2);

			assertThat(subject.keySet()).containsExactly(id);
		}

		verify(expected2).close();

		// Cache should now be empty
		assertThat(subject.keySet()).isEmpty();
	}

	@Test
	public void findSessionAsync() {
		SessionManager<Void> manager = mock(SessionManager.class);
		CachedSessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.CONCURRENT);
		CompletableFuture<Session<Void>> future1 = new CompletableFuture<>();
		CompletableFuture<Session<Void>> future2 = new CompletableFuture<>();
		Session<Void> expected1 = mock(Session.class);
		Session<Void> expected2 = mock(Session.class);
		String id = "foo";
		SessionMetaData metaData1 = mock(SessionMetaData.class);
		SessionAttributes attributes1 = mock(SessionAttributes.class);
		SessionMetaData metaData2 = mock(SessionMetaData.class);
		SessionAttributes attributes2 = mock(SessionAttributes.class);

		doReturn(future1, future2).when(manager).findSessionAsync(id);

		doReturn(id).when(expected1).getId();
		doReturn(true).when(expected1).isValid();
		doReturn(attributes1).when(expected1).getAttributes();
		doReturn(metaData1).when(expected1).getMetaData();
		doReturn(id).when(expected2).getId();
		doReturn(true).when(expected2).isValid();
		doReturn(attributes2).when(expected2).getAttributes();
		doReturn(metaData2).when(expected2).getMetaData();

		CompletionStage<Session<Void>> stage1 = subject.findSessionAsync(id);

		verify(manager).findSessionAsync(id);

		assertThat(subject.keySet()).containsExactly(id);

		CompletionStage<Session<Void>> stage2 = subject.findSessionAsync(id);

		// Should return the same session without invoking the manager
		verifyNoMoreInteractions(manager);

		assertThat(subject.keySet()).containsExactly(id);

		future1.complete(expected1);

		try (Session<Void> session = stage1.toCompletableFuture().join()) {
			assertThat(session).isNotNull();
			assertThat(session.getId()).isSameAs(id);
			assertThat(session.getMetaData()).isSameAs(metaData1);
			assertThat(session.getAttributes()).isSameAs(attributes1);
		}

		// Should not trigger Session.close() yet
		verify(expected1, never()).close();

		try (Session<Void> session = stage2.toCompletableFuture().join()) {
			assertThat(session).isNotNull();
			assertThat(session.getId()).isSameAs(id);
			assertThat(session.getMetaData()).isSameAs(metaData1);
			assertThat(session.getAttributes()).isSameAs(attributes1);
		}

		verify(expected1).close();

		// Cache should now be empty
		assertThat(subject.keySet()).isEmpty();

		CompletionStage<Session<Void>> stage = subject.findSessionAsync(id);

		assertThat(subject.keySet()).containsExactly(id);

		future2.complete(expected2);

		try (Session<Void> session = stage.toCompletableFuture().join()) {
			// Should use second session instance
			verify(manager, times(2)).findSessionAsync(id);

			assertThat(session).isNotNull();
			assertThat(session.getId()).isSameAs(id);
			assertThat(session.getMetaData()).isSameAs(metaData2);
			assertThat(session.getAttributes()).isSameAs(attributes2);

			assertThat(subject.keySet()).containsExactly(id);
		}

		verify(expected2).close();

		// Cache should now be empty
		assertThat(subject.keySet()).isEmpty();
	}

	@Test
	public void findMissingSession() {
		SessionManager<Void> manager = mock(SessionManager.class);
		CachedSessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.CONCURRENT);
		String id = "foo";

		doReturn(CompletableFuture.completedStage(null)).when(manager).findSessionAsync(id);

		assertThat(subject.findSession(id)).isNull();

		verify(manager).findSessionAsync(id);

		assertThat(subject.keySet()).isEmpty();

		assertThat(subject.findSession(id)).isNull();

		verify(manager, times(2)).findSessionAsync(id);

		// Cache should now be empty
		assertThat(subject.keySet()).isEmpty();
	}

	@Test
	public void findMissingSessionAsync() {
		SessionManager<Void> manager = mock(SessionManager.class);
		CachedSessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.CONCURRENT);
		CompletableFuture<Session<Void>> future = new CompletableFuture<>();
		String id = "foo";

		doReturn(future).when(manager).findSessionAsync(id);

		CompletionStage<Session<Void>> stage1 = subject.findSessionAsync(id);

		verify(manager).findSessionAsync(id);

		assertThat(subject.keySet()).containsExactly(id);

		CompletionStage<Session<Void>> stage2 = subject.findSessionAsync(id);

		verifyNoMoreInteractions(manager);

		assertThat(subject.keySet()).containsExactly(id);

		future.complete(null);

		assertThat(stage1).isCompletedWithValue(null);
		assertThat(stage2).isCompletedWithValue(null);

		// Cache should now be empty
		assertThat(subject.keySet()).isEmpty();
	}

	@Test
	public void findInvalidSession() {
		SessionManager<Void> manager = mock(SessionManager.class);
		CachedSessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.CONCURRENT);
		String id = "foo";
		Session<Void> expected = mock(Session.class);
		SessionMetaData metaData = mock(SessionMetaData.class);
		SessionAttributes attributes = mock(SessionAttributes.class);

		doReturn(CompletableFuture.completedStage(expected)).when(manager).findSessionAsync(id);

		doReturn(id).when(expected).getId();
		doReturn(true).when(expected).isValid();
		doReturn(attributes).when(expected).getAttributes();
		doReturn(metaData).when(expected).getMetaData();

		try (Session<Void> session1 = subject.findSession(id)) {
			verify(manager).findSessionAsync(id);

			assertThat(session1).isNotNull();
			assertThat(session1.getId()).isSameAs(id);
			assertThat(session1.getMetaData()).isSameAs(metaData);
			assertThat(session1.getAttributes()).isSameAs(attributes);

			assertThat(subject.keySet()).containsExactly(id);

			// Simulate invalidation
			doReturn(false).when(expected).isValid();

			// Subsequent requests should not find session
			assertThat(subject.findSession(id)).isNull();

			// Should not trigger Session.close() yet
			verify(expected, never()).close();

			assertThat(subject.keySet()).containsExactly(id);
		}

		verify(expected).close();

		// Cache should now be empty
		assertThat(subject.keySet()).isEmpty();

		// Subsequent requests should not find session
		assertThat(subject.findSession(id)).isNull();

		// Cache should still be empty
		assertThat(subject.keySet()).isEmpty();
	}

	@Test
	public void findInvalidSessionAsync() {
		SessionManager<Void> manager = mock(SessionManager.class);
		CachedSessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.CONCURRENT);
		CompletableFuture<Session<Void>> future = new CompletableFuture<>();
		String id = "foo";
		Session<Void> expected = mock(Session.class);
		SessionMetaData metaData = mock(SessionMetaData.class);
		SessionAttributes attributes = mock(SessionAttributes.class);

		doReturn(future).when(manager).findSessionAsync(id);

		doReturn(id).when(expected).getId();
		doReturn(true).when(expected).isValid();
		doReturn(attributes).when(expected).getAttributes();
		doReturn(metaData).when(expected).getMetaData();

		CompletionStage<Session<Void>> stage1 = subject.findSessionAsync(id);

		verify(manager).findSessionAsync(id);

		assertThat(subject.keySet()).containsExactly(id);

		CompletionStage<Session<Void>> stage2 = subject.findSessionAsync(id);

		verifyNoMoreInteractions(manager);

		assertThat(subject.keySet()).containsExactly(id);

		future.complete(expected);

		// Simulate invalidation
		doReturn(false).when(expected).isValid();

		try (Session<Void> session1 = stage1.toCompletableFuture().join()) {

			assertThat(session1.isValid()).isFalse();

			// Should not trigger Session.close() yet
			verify(expected, never()).close();

			try (Session<Void> session2 = stage2.toCompletableFuture().join()) {

				assertThat(session2.isValid()).isFalse();

				// Should not trigger Session.close() yet
				verify(expected, never()).close();

				// Subsequent requests should not find session
				assertThat(subject.findSessionAsync(id)).isCompletedWithValue(null);

				// Should not trigger Session.close() yet
				verify(expected, never()).close();
			}

			// Should not trigger Session.close() yet
			verify(expected, never()).close();
		}

		// Should not trigger Session.close() yet
		verify(expected).close();

		// Cache should now be empty
		assertThat(subject.keySet()).isEmpty();

		// Subsequent requests should not find session
		assertThat(subject.findSessionAsync(id)).isCompletedWithValue(null);

		// Cache should still be empty
		assertThat(subject.keySet()).isEmpty();
	}

	@Test
	public void findExceptionalSession() {
		SessionManager<Void> manager = mock(SessionManager.class);
		CachedSessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.CONCURRENT);
		String id = "foo";

		doReturn(CompletableFuture.failedFuture(new Exception())).when(manager).findSessionAsync(id);

		assertThatThrownBy(() -> subject.findSession(id));

		verify(manager).findSessionAsync(id);

		assertThat(subject.keySet()).isEmpty();

		assertThatThrownBy(() -> subject.findSession(id));

		verify(manager, times(2)).findSessionAsync(id);

		// Cache should now be empty
		assertThat(subject.keySet()).isEmpty();
	}

	@Test
	public void findExceptionalSessionAsync() {
		SessionManager<Void> manager = mock(SessionManager.class);
		CachedSessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.CONCURRENT);
		CompletableFuture<Session<Void>> future = new CompletableFuture<>();
		String id = "foo";

		doReturn(future).when(manager).findSessionAsync(id);

		CompletionStage<Session<Void>> stage1 = subject.findSessionAsync(id);

		verify(manager).findSessionAsync(id);

		assertThat(subject.keySet()).containsExactly(id);

		CompletionStage<Session<Void>> stage2 = subject.findSessionAsync(id);

		verifyNoMoreInteractions(manager);

		assertThat(subject.keySet()).containsExactly(id);

		future.completeExceptionally(new Exception());

		assertThat(stage1).isCompletedExceptionally();
		assertThat(stage2).isCompletedExceptionally();

		// Cache should now be empty
		assertThat(subject.keySet()).isEmpty();
	}

	@Test
	public void createSession() {
		SessionManager<Void> manager = mock(SessionManager.class);
		CachedSessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.CONCURRENT);
		Session<Void> expected1 = mock(Session.class);
		Session<Void> expected2 = mock(Session.class);
		String id = "foo";
		Instant creationTime = Instant.now();
		SessionMetaData metaData1 = mock(SessionMetaData.class);
		SessionAttributes attributes1 = mock(SessionAttributes.class);
		SessionMetaData metaData2 = mock(SessionMetaData.class);
		SessionAttributes attributes2 = mock(SessionAttributes.class);

		doReturn(CompletableFuture.completedStage(expected1), CompletableFuture.completedStage(expected2)).when(manager).createSessionAsync(id, creationTime);
		doReturn(id).when(expected1).getId();
		doReturn(true).when(expected1).isValid();
		doReturn(attributes1).when(expected1).getAttributes();
		doReturn(metaData1).when(expected1).getMetaData();
		doReturn(id).when(expected2).getId();
		doReturn(true).when(expected2).isValid();
		doReturn(attributes2).when(expected2).getAttributes();
		doReturn(metaData2).when(expected2).getMetaData();

		try (Session<Void> session1 = subject.createSession(id, creationTime)) {
			verify(manager).createSessionAsync(id, creationTime);

			assertThat(session1).isNotNull();
			assertThat(session1.getId()).isSameAs(id);
			assertThat(session1.getMetaData()).isSameAs(metaData1);
			assertThat(session1.getAttributes()).isSameAs(attributes1);

			assertThat(subject.keySet()).containsExactly(id);

			try (Session<Void> session2 = subject.findSession(id)) {
				// Should return the same session without invoking the manager
				verifyNoMoreInteractions(manager);

				assertThat(session2).isNotNull();
				assertThat(session2).isSameAs(session1);

				assertThat(subject.keySet()).containsExactly(id);
			}

			// Should not trigger Session.close() yet
			verify(expected1, never()).close();

			assertThat(subject.keySet()).containsExactly(id);
		}

		verify(expected1).close();

		// Cache should now be empty
		assertThat(subject.keySet()).isEmpty();

		// Should use second session instance
		try (Session<Void> session = subject.createSession(id, creationTime)) {
			verify(manager, times(2)).createSessionAsync(id, creationTime);

			assertThat(session).isNotNull();
			assertThat(session.getId()).isSameAs(id);
			assertThat(session.getMetaData()).isSameAs(metaData2);
			assertThat(session.getAttributes()).isSameAs(attributes2);

			assertThat(subject.keySet()).containsExactly(id);
		}

		verify(expected2).close();

		// Cache should now be empty
		assertThat(subject.keySet()).isEmpty();
	}

	@Test
	public void createSessionAsync() {
		SessionManager<Void> manager = mock(SessionManager.class);
		CachedSessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.CONCURRENT);
		CompletableFuture<Session<Void>> future1 = new CompletableFuture<>();
		CompletableFuture<Session<Void>> future2 = new CompletableFuture<>();
		Session<Void> expected1 = mock(Session.class);
		Session<Void> expected2 = mock(Session.class);
		String id = "foo";
		Instant creationTime = Instant.now();
		SessionMetaData metaData1 = mock(SessionMetaData.class);
		SessionAttributes attributes1 = mock(SessionAttributes.class);
		SessionMetaData metaData2 = mock(SessionMetaData.class);
		SessionAttributes attributes2 = mock(SessionAttributes.class);

		doReturn(future1, future2).when(manager).createSessionAsync(id, creationTime);

		doReturn(id).when(expected1).getId();
		doReturn(true).when(expected1).isValid();
		doReturn(attributes1).when(expected1).getAttributes();
		doReturn(metaData1).when(expected1).getMetaData();
		doReturn(id).when(expected2).getId();
		doReturn(true).when(expected2).isValid();
		doReturn(attributes2).when(expected2).getAttributes();
		doReturn(metaData2).when(expected2).getMetaData();

		CompletionStage<Session<Void>> stage1 = subject.createSessionAsync(id, creationTime);

		verify(manager).createSessionAsync(id, creationTime);

		assertThat(subject.keySet()).containsExactly(id);

		CompletionStage<Session<Void>> stage2 = subject.createSessionAsync(id, creationTime);

		// Should return the same session without invoking the manager
		verifyNoMoreInteractions(manager);

		assertThat(subject.keySet()).containsExactly(id);

		future1.complete(expected1);

		try (Session<Void> session = stage1.toCompletableFuture().join()) {
			assertThat(session).isNotNull();
			assertThat(session.getId()).isSameAs(id);
			assertThat(session.getMetaData()).isSameAs(metaData1);
			assertThat(session.getAttributes()).isSameAs(attributes1);
		}

		// Should not trigger Session.close() yet
		verify(expected1, never()).close();

		try (Session<Void> session = stage2.toCompletableFuture().join()) {
			assertThat(session).isNotNull();
			assertThat(session.getId()).isSameAs(id);
			assertThat(session.getMetaData()).isSameAs(metaData1);
			assertThat(session.getAttributes()).isSameAs(attributes1);
		}

		verify(expected1).close();

		// Cache should now be empty
		assertThat(subject.keySet()).isEmpty();

		CompletionStage<Session<Void>> stage = subject.createSessionAsync(id, creationTime);

		verify(manager, times(2)).createSessionAsync(id, creationTime);

		assertThat(subject.keySet()).containsExactly(id);

		future2.complete(expected2);

		try (Session<Void> session = stage.toCompletableFuture().join()) {
			assertThat(session).isNotNull();
			assertThat(session.getId()).isSameAs(id);
			assertThat(session.getMetaData()).isSameAs(metaData2);
			assertThat(session.getAttributes()).isSameAs(attributes2);

			assertThat(subject.keySet()).containsExactly(id);
		}

		verify(expected2).close();

		// Cache should now be empty
		assertThat(subject.keySet()).isEmpty();
	}

	@Test
	public void createExceptionalSession() {
		SessionManager<Void> manager = mock(SessionManager.class);
		CachedSessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.CONCURRENT);
		String id = "foo";
		Instant creationTime = Instant.now();

		doReturn(CompletableFuture.failedFuture(new Exception())).when(manager).createSessionAsync(id, creationTime);

		assertThatThrownBy(() -> subject.createSession(id, creationTime));

		verify(manager).createSessionAsync(id, creationTime);

		assertThat(subject.keySet()).isEmpty();

		assertThatThrownBy(() -> subject.createSession(id, creationTime));

		verify(manager, times(2)).createSessionAsync(id, creationTime);

		// Cache should now be empty
		assertThat(subject.keySet()).isEmpty();
	}

	@Test
	public void createExceptionalSessionAsync() {
		SessionManager<Void> manager = mock(SessionManager.class);
		CachedSessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.CONCURRENT);
		CompletableFuture<Session<Void>> future = new CompletableFuture<>();
		String id = "foo";
		Instant creationTime = Instant.now();

		doReturn(future).when(manager).createSessionAsync(id, creationTime);

		CompletionStage<Session<Void>> stage1 = subject.createSessionAsync(id, creationTime);

		verify(manager).createSessionAsync(id, creationTime);

		assertThat(subject.keySet()).containsExactly(id);

		CompletionStage<Session<Void>> stage2 = subject.createSessionAsync(id, creationTime);

		verifyNoMoreInteractions(manager);

		assertThat(subject.keySet()).containsExactly(id);

		future.completeExceptionally(new Exception());

		assertThat(stage1).isCompletedExceptionally();
		assertThat(stage2).isCompletedExceptionally();

		// Cache should now be empty
		assertThat(subject.keySet()).isEmpty();
	}

	@Test
	public void getIdentifierFactory() {
		SessionManager<Void> manager = mock(SessionManager.class);
		SessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.NONE);
		Supplier<String> expected = mock(Supplier.class);

		doReturn(expected).when(manager).getIdentifierFactory();

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

		doReturn(statistics).when(manager).getStatistics();

		assertThat(subject.getStatistics()).isSameAs(statistics);
	}

	@Test
	public void getBatcher() {
		SessionManager<Void> manager = mock(SessionManager.class);
		SessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.NONE);
		Supplier<Batch> expected = mock(Supplier.class);

		doReturn(expected).when(manager).getBatchFactory();

		Supplier<Batch> result = subject.getBatchFactory();

		assertThat(result).isSameAs(expected);
	}

	@Test
	public void findImmutableSession() {
		SessionManager<Void> manager = mock(SessionManager.class);
		SessionManager<Void> subject = new CachedSessionManager<>(manager, CacheStrategy.NONE);
		ImmutableSession expected = mock(ImmutableSession.class);
		String id = "foo";

		doReturn(CompletableFuture.completedStage(expected)).when(manager).findImmutableSessionAsync(id);

		ImmutableSession result = subject.findImmutableSession(id);

		assertThat(result).isSameAs(expected);
	}
}
