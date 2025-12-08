/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.context.DefaultThreadFactory;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.server.util.MapEntry;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionManagerConfiguration;
import org.wildfly.clustering.session.SessionManagerFactory;
import org.wildfly.clustering.session.SessionMetaData;

/**
 * Session manager integration tests.
 * @param <P> the parameters type
 * @author Paul Ferraro
 */
public abstract class SessionManagerITCase<P extends SessionManagerParameters> {
	private static final Supplier<AtomicReference<String>> SESSION_CONTEXT_FACTORY = AtomicReference::new;
	private static final String DEPLOYMENT_CONTEXT = "deployment";

	private final System.Logger logger = System.getLogger(this.getClass().getName());
	private final SessionManagerFactoryContextProvider<P, String> factory;
	private final String threadGroupName = this.getClass().getSimpleName();

	protected SessionManagerITCase(SessionManagerFactoryContextProvider<P, String> factory) {
		this.factory = factory;
	}

	protected void basic(P parameters) {
		BlockingQueue<ImmutableSession> expiredSessions = new LinkedBlockingQueue<>();
		SessionManagerConfiguration<String> managerConfig1 = new TestSessionManagerConfiguration<>(expiredSessions, DEPLOYMENT_CONTEXT);
		SessionManagerConfiguration<String> managerConfig2 = new TestSessionManagerConfiguration<>(expiredSessions, DEPLOYMENT_CONTEXT);
		try (Context<SessionManagerFactory<String, AtomicReference<String>>> factory1Context = this.factory.createContext(parameters, "member1", SESSION_CONTEXT_FACTORY)) {
			SessionManagerFactory<String, AtomicReference<String>> factory1 = factory1Context.get();
			SessionManager<AtomicReference<String>> manager1 = factory1.createSessionManager(managerConfig1);
			manager1.start();
			try (Context<SessionManagerFactory<String, AtomicReference<String>>> factory2Context = this.factory.createContext(parameters, "member2", SESSION_CONTEXT_FACTORY)) {
				SessionManagerFactory<String, AtomicReference<String>> factory2 = factory2Context.get();
				SessionManager<AtomicReference<String>> manager2 = factory2.createSessionManager(managerConfig2);
				manager2.start();
				try {
					String sessionId = manager1.getIdentifierFactory().get();
					this.verifyNoSession(manager1, sessionId);
					this.verifyNoSession(manager2, sessionId);
					UUID foo = UUID.randomUUID();
					UUID bar = UUID.randomUUID();

					this.createSession(manager1, sessionId, Map.of("foo", foo, "bar", bar));

					this.verifySession(manager1, sessionId, Map.of("foo", foo, "bar", bar));
					this.verifySession(manager2, sessionId, Map.of("foo", foo, "bar", bar));

					this.updateSession(manager1, sessionId, Map.of("foo", MapEntry.of(foo, null), "bar", Map.entry(bar, 0)));

					for (int i = 1; i <= 20; i += 2) {
						this.updateSession(manager1, sessionId, Map.of("bar", Map.entry(i - 1, i)));
						this.updateSession(manager2, sessionId, Map.of("bar", Map.entry(i, i + 1)));
					}

					this.invalidateSession(manager1, sessionId);

					this.verifyNoSession(manager1, sessionId);
					this.verifyNoSession(manager2, sessionId);

					assertThat(expiredSessions).isEmpty();
				} finally {
					manager2.stop();
				}
			} finally {
				manager1.stop();
			}
		}
	}

	protected void concurrent(P parameters) throws InterruptedException, ExecutionException {
		int threads = 10;
		int requests = 10;
		BlockingQueue<ImmutableSession> expiredSessions = new LinkedBlockingQueue<>();
		SessionManagerConfiguration<String> managerConfig1 = new TestSessionManagerConfiguration<>(expiredSessions, DEPLOYMENT_CONTEXT);
		SessionManagerConfiguration<String> managerConfig2 = new TestSessionManagerConfiguration<>(expiredSessions, DEPLOYMENT_CONTEXT);
		try (Context<SessionManagerFactory<String, AtomicReference<String>>> factory1Context = this.factory.createContext(parameters, "member1", SESSION_CONTEXT_FACTORY)) {
			SessionManagerFactory<String, AtomicReference<String>> factory1 = factory1Context.get();
			SessionManager<AtomicReference<String>> manager1 = factory1.createSessionManager(managerConfig1);
			manager1.start();
			try (Context<SessionManagerFactory<String, AtomicReference<String>>> factory2Context = this.factory.createContext(parameters, "member2", SESSION_CONTEXT_FACTORY)) {
				SessionManagerFactory<String, AtomicReference<String>> factory2 = factory2Context.get();
				SessionManager<AtomicReference<String>> manager2 = factory2.createSessionManager(managerConfig2);
				manager2.start();
				try {
					String sessionId = manager1.getIdentifierFactory().get();
					AtomicInteger value = new AtomicInteger();

					this.createSession(manager1, sessionId, Map.of("value", value));

					List<Runnable> tasks = new ArrayList<>(threads);
					// Trigger a number of concurrent sequences of requests for the same session
					for (int i = 0; i < threads; ++i) {
						tasks.add(() -> {
							for (int j = 0; j < requests; ++j) {
								this.requestSession(manager1, sessionId, session -> {
									AtomicInteger v = (AtomicInteger) session.getAttributes().get("value");
									assertThat(v).isNotNull();
									v.incrementAndGet();
								});
							}
						});
					}
					List<Future<?>> futures = new ArrayList<>(threads);
					ExecutorService executor = Executors.newFixedThreadPool(threads, new DefaultThreadFactory(new ThreadGroup(this.threadGroupName), Thread.currentThread().getContextClassLoader()));
					try {
						Instant start = Instant.now();
						for (Runnable task : tasks) {
							futures.add(executor.submit(task));
						}
						for (Future<?> future : futures) {
							future.get();
						}
						Instant stop = Instant.now();
						Duration concurrentDuration = Duration.between(start, stop);
						this.logger.log(System.Logger.Level.INFO, "{0} concurrent requests completed in {1}", threads * requests, concurrentDuration);

						// Verify integrity of value on other manager
						this.requestSession(manager2, sessionId, session -> {
							assertThat((AtomicInteger) session.getAttributes().get("value")).hasValue(threads * requests);
						});

						start = Instant.now();
						// Trigger sequences of the same number of requests for the same session
						for (int i = 0; i < threads; ++i) {
							for (int j = 0; j < requests; ++j) {
								this.requestSession(manager1, sessionId, session -> {
									AtomicInteger v = (AtomicInteger) session.getAttributes().get("value");
									assertThat(v).isNotNull();
									v.incrementAndGet();
								});
							}
						}
						stop = Instant.now();
						Duration serialDuration = Duration.between(start, stop);
						this.logger.log(System.Logger.Level.INFO, "{0} serial requests completed in {1}", threads * requests, serialDuration);

						// Verify integrity of value on other manager
						this.requestSession(manager2, sessionId, session -> {
							assertThat((AtomicInteger) session.getAttributes().get("value")).hasValue(threads * requests * 2);
						});

						// Ensure that concurrent requests complete faster than same number of serial requests
						assertThat(concurrentDuration).isLessThan(serialDuration);
					} finally {
						executor.shutdown();
					}

					this.invalidateSession(manager2, sessionId);
				} finally {
					manager2.stop();
				}
			} finally {
				manager1.stop();
			}
		}
	}

	protected void expiration(P parameters) throws InterruptedException {
		Duration expirationDuration = Duration.ofSeconds(120);
		BlockingQueue<ImmutableSession> expiredSessions = new LinkedBlockingQueue<>();
		SessionManagerConfiguration<String> managerConfig1 = new TestSessionManagerConfiguration<>(expiredSessions, DEPLOYMENT_CONTEXT);
		SessionManagerConfiguration<String> managerConfig2 = new TestSessionManagerConfiguration<>(expiredSessions, DEPLOYMENT_CONTEXT);
		try (Context<SessionManagerFactory<String, AtomicReference<String>>> factory1Context = this.factory.createContext(parameters, "member1", SESSION_CONTEXT_FACTORY)) {
			SessionManagerFactory<String, AtomicReference<String>> factory1 = factory1Context.get();
			SessionManager<AtomicReference<String>> manager1 = factory1.createSessionManager(managerConfig1);
			manager1.start();
			try (Context<SessionManagerFactory<String, AtomicReference<String>>> factory2Context = this.factory.createContext(parameters, "member2", SESSION_CONTEXT_FACTORY)) {
				SessionManagerFactory<String, AtomicReference<String>> factory2 = factory2Context.get();
				SessionManager<AtomicReference<String>> manager2 = factory2.createSessionManager(managerConfig2);
				manager2.start();
				try {
					String sessionId = manager1.getIdentifierFactory().get();
					UUID foo = UUID.randomUUID();
					UUID bar = UUID.randomUUID();

					this.createSession(manager1, sessionId, Map.of("foo", foo, "bar", bar));

					// Setup session to expire soon
					this.requestSession(manager1, sessionId, session -> {
						session.getMetaData().setMaxIdle(Duration.ofSeconds(2));
					});

					// Verify that session does not expire prematurely
					TimeUnit.SECONDS.sleep(1);

					this.verifySession(manager2, sessionId, Map.of("foo", foo, "bar", bar));

					// Verify that session does not expire prematurely
					TimeUnit.SECONDS.sleep(1);

					this.verifySession(manager2, sessionId, Map.of("foo", foo, "bar", bar));

					// Verify that session does not expire prematurely
					TimeUnit.SECONDS.sleep(1);

					this.verifySession(manager2, sessionId, Map.of("foo", foo, "bar", bar));

					try {
						Instant start = Instant.now();
						ImmutableSession expiredSession = expiredSessions.poll(expirationDuration.getSeconds(), TimeUnit.SECONDS);
						assertThat(expiredSession).as("No expiration event received within %s seconds", expirationDuration.getSeconds()).isNotNull();
						this.logger.log(System.Logger.Level.INFO, "Received expiration event for {0} after {1}", expiredSession.getId(), Duration.between(start, Instant.now()));
						assertThat(sessionId).isEqualTo(expiredSession.getId());
						assertThat(expiredSession.isValid()).isFalse();
						assertThat(expiredSession.getMetaData().getLastAccessTime()).isPresent();
						assertThat(expiredSession.getMetaData().isExpired()).isTrue();
						assertThat(expiredSession.getMetaData().getMaxIdle()).isPresent();
						verifySessionAttributes(expiredSession, Map.of("foo", foo, "bar", bar));

						assertThat(expiredSessions).isEmpty();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}

					this.verifyNoSession(manager1, sessionId);
					this.verifyNoSession(manager2, sessionId);
				} finally {
					manager2.stop();
				}
			} finally {
				manager1.stop();
			}
		}
	}

	private void createSession(SessionManager<AtomicReference<String>> manager, String sessionId, Map<String, Object> attributes) {
		this.requestSession(manager, manager::createSession, sessionId, session -> {
			assertThat(session.getMetaData().getLastAccessTime()).isEmpty();
			verifySessionMetaData(session);
			verifySessionAttributes(session, Map.of());
			updateSessionAttributes(session, attributes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> new AbstractMap.SimpleEntry<>(null, entry.getValue()))));
			verifySessionAttributes(session, attributes);
		});
	}

	private void verifySession(SessionManager<AtomicReference<String>> manager, String sessionId, Map<String, Object> attributes) {
		this.requestSession(manager, sessionId, session -> {
			assertThat(session.getMetaData().getLastAccessTime()).isPresent();
			verifySessionMetaData(session);
			verifySessionAttributes(session, attributes);
		});
	}

	private void updateSession(SessionManager<AtomicReference<String>> manager, String sessionId, Map<String, Map.Entry<Object, Object>> attributes) {
		this.requestSession(manager, sessionId, session -> {
			assertThat(session).isNotNull();
			assertThat(session.getId()).isEqualTo(sessionId);
			assertThat(session.getMetaData().getLastAccessTime()).isPresent();
			verifySessionMetaData(session);
			updateSessionAttributes(session, attributes);
		});
	}

	private void invalidateSession(SessionManager<AtomicReference<String>> manager, String sessionId) {
		this.requestSession(manager, sessionId, session -> {
			session.invalidate();
			assertThat(session.isValid()).isFalse();
			assertThatThrownBy(() -> session.getAttributes()).isInstanceOf(IllegalStateException.class);
			assertThatThrownBy(() -> session.getMetaData()).isInstanceOf(IllegalStateException.class);
		});
	}

	private void requestSession(SessionManager<AtomicReference<String>> manager, String sessionId, Consumer<Session<AtomicReference<String>>> action) {
		this.requestSession(manager, manager::findSession, sessionId, action);
	}

	private void requestSession(SessionManager<AtomicReference<String>> manager, Function<String, Session<AtomicReference<String>>> sessionFactory, String sessionId, Consumer<Session<AtomicReference<String>>> action) {
		Instant start = Instant.now();
		try (Batch batch = manager.getBatchFactory().get()) {
			try (Session<AtomicReference<String>> session = sessionFactory.apply(sessionId)) {
				assertThat(session).isNotNull();
				assertThat(session.getId()).isEqualTo(sessionId);
				action.accept(session);
				// Post-request processing
				if (session.isValid()) {
					SessionMetaData metaData = session.getMetaData();
					Instant end = Instant.now();
					metaData.setLastAccess(start, end);
					// Once last-access is set, session should no longer be "new"
					assertThat(session.getMetaData().getLastAccessTime()).isPresent();
					assertThat(session.getMetaData().getLastAccessStartTime()).isPresent();
					assertThat(session.getMetaData().getLastAccessEndTime()).isPresent();
					// Skip these assertions during concurrent session access
					// We would otherwise require memory synchronization to validate
					if (!Thread.currentThread().getThreadGroup().getName().equals(this.threadGroupName)) {
						// Validate last-access times are within precision bounds
						if (metaData.getLastAccessStartTime().isPresent()) {
							assertThat(Duration.between(metaData.getLastAccessStartTime().get(), start).getSeconds()).isEqualTo(0);
							assertThat(Duration.between(metaData.getLastAccessStartTime().get(), start).truncatedTo(ChronoUnit.MILLIS).getNano()).isEqualTo(0);
						}
						if (metaData.getLastAccessEndTime().isPresent()) {
							assertThat(Duration.between(end, metaData.getLastAccessEndTime().get()).getSeconds()).isEqualTo(0);
						}
					}
				}
			} catch (RuntimeException e) {
				this.logger.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
				batch.discard();
				throw e;
			}
		}
	}

	private static void verifySessionMetaData(Session<AtomicReference<String>> session) {
		assertThat(session.isValid()).isTrue();
		SessionMetaData metaData = session.getMetaData();
		assertThat(metaData.getMaxIdle()).isPresent();
		assertThat(metaData.isExpired()).isFalse();
		if (metaData.getLastAccessTime().isPresent()) {
			// For the request following session creation, the last access time will precede the creation time, but this duration should be tiny
			if (metaData.getLastAccessStartTime().get().isBefore(metaData.getCreationTime())) {
				assertThat(Duration.between(metaData.getLastAccessStartTime().get(), metaData.getCreationTime()).getSeconds()).isEqualTo(0);
			}
			assertThat(metaData.getLastAccessStartTime().get()).isBefore(metaData.getLastAccessEndTime().get());
		}
	}

	private static void verifySessionAttributes(ImmutableSession session, Map<String, Object> attributes) {
		assertThat(session.getAttributes().keySet()).containsAll(attributes.keySet());
		for (Map.Entry<String, Object> entry : attributes.entrySet()) {
			assertThat(session.getAttributes().get(entry.getKey())).isEqualTo(entry.getValue());
		}
	}

	private static void updateSessionAttributes(Session<AtomicReference<String>> session, Map<String, Map.Entry<Object, Object>> attributes) {
		for (Map.Entry<String, Map.Entry<Object, Object>> entry : attributes.entrySet()) {
			String name = entry.getKey();
			Object expected = entry.getValue().getKey();
			Object value = entry.getValue().getValue();
			if (value != null) {
				assertThat(session.getAttributes().put(name, value)).isEqualTo(expected);
			} else {
				assertThat(session.getAttributes().remove(name)).isEqualTo(expected);
			}
		}
	}

	private void verifyNoSession(SessionManager<AtomicReference<String>> manager, String sessionId) {
		try (Batch batch = manager.getBatchFactory().get()) {
			try (Session<AtomicReference<String>> session = manager.findSession(sessionId)) {
				assertThat(session).isNull();
			} catch (RuntimeException e) {
				this.logger.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
				batch.discard();
				throw e;
			}
		}
	}

	private static class TestSessionManagerConfiguration<C> implements SessionManagerConfiguration<C> {
		private final Consumer<ImmutableSession> expirationListener;
		private final C context;

		TestSessionManagerConfiguration(BlockingQueue<ImmutableSession> expired, C context) {
			org.wildfly.clustering.function.Consumer<ImmutableSession> queue = expired::add;
			this.expirationListener = queue.compose(SimpleImmutableSession::new);
			this.context = context;
		}

		@Override
		public Supplier<String> getIdentifierFactory() {
			return () -> UUID.randomUUID().toString();
		}

		@Override
		public Consumer<ImmutableSession> getExpirationListener() {
			return this.expirationListener;
		}

		@Override
		public Optional<Duration> getMaxIdle() {
			return Optional.of(Duration.ofMinutes(1));
		}

		@Override
		public C getContext() {
			return this.context;
		}
	}
}
