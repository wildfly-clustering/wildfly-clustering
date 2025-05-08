/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.function.BiConsumer;
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

	private static final String DEPLOYMENT_CONTEXT = "deployment";
	private static final Supplier<AtomicReference<String>> SESSION_CONTEXT_FACTORY = AtomicReference::new;

	private final System.Logger logger = System.getLogger(this.getClass().getName());
	private final BiFunction<P, String, SessionManagerFactoryProvider<String>> factory;

	protected SessionManagerITCase(BiFunction<P, String, SessionManagerFactoryProvider<String>> factory) {
		this.factory = factory;
	}

	protected void basic(P parameters) throws Exception {
		BlockingQueue<ImmutableSession> expiredSessions = new LinkedBlockingQueue<>();
		SessionManagerConfiguration<String> managerConfig1 = new TestSessionManagerConfiguration<>(expiredSessions, DEPLOYMENT_CONTEXT);
		SessionManagerConfiguration<String> managerConfig2 = new TestSessionManagerConfiguration<>(expiredSessions, DEPLOYMENT_CONTEXT);
		try (SessionManagerFactoryProvider<String> provider1 = this.factory.apply(parameters, "member1")) {
			try (SessionManagerFactory<String, AtomicReference<String>> factory1 = provider1.createSessionManagerFactory(SESSION_CONTEXT_FACTORY)) {
				SessionManager<AtomicReference<String>> manager1 = factory1.createSessionManager(managerConfig1);
				manager1.start();
				try (SessionManagerFactoryProvider<String> provider2 = this.factory.apply(parameters, "member2")) {
					try (SessionManagerFactory<String, AtomicReference<String>> factory2 = provider2.createSessionManagerFactory(SESSION_CONTEXT_FACTORY)) {
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

							this.updateSession(manager1, sessionId, Map.of("foo", new AbstractMap.SimpleEntry<>(foo, null), "bar", Map.entry(bar, 0)));

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
					}
				} finally {
					manager1.stop();
				}
			}
		}
	}

	protected void concurrent(P parameters) throws Exception {
		int threads = 10;
		int requests = 10;
		BlockingQueue<ImmutableSession> expiredSessions = new LinkedBlockingQueue<>();
		SessionManagerConfiguration<String> managerConfig1 = new TestSessionManagerConfiguration<>(expiredSessions, DEPLOYMENT_CONTEXT);
		SessionManagerConfiguration<String> managerConfig2 = new TestSessionManagerConfiguration<>(expiredSessions, DEPLOYMENT_CONTEXT);
		try (SessionManagerFactoryProvider<String> provider1 = this.factory.apply(parameters, "member1")) {
			try (SessionManagerFactory<String, AtomicReference<String>> factory1 = provider1.createSessionManagerFactory(SESSION_CONTEXT_FACTORY)) {
				SessionManager<AtomicReference<String>> manager1 = factory1.createSessionManager(managerConfig1);
				manager1.start();
				try (SessionManagerFactoryProvider<String> provider2 = this.factory.apply(parameters, "member2")) {
					try (SessionManagerFactory<String, AtomicReference<String>> factory2 = provider2.createSessionManagerFactory(SESSION_CONTEXT_FACTORY)) {
						SessionManager<AtomicReference<String>> manager2 = factory2.createSessionManager(managerConfig2);
						manager2.start();
						try {
							String sessionId = manager1.getIdentifierFactory().get();
							AtomicInteger value = new AtomicInteger();

							this.createSession(manager1, sessionId, Map.of("value", value));

							Instant start = Instant.now();
							// Trigger a number of concurrent sequences of requests for the same session
							CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
							for (int i = 0; i < threads; ++i) {
								future = future.thenAcceptBoth(CompletableFuture.runAsync(() -> {
									for (int j = 0; j < requests; ++j) {
										this.requestSession(manager1, sessionId, session -> {
											AtomicInteger v = (AtomicInteger) session.getAttributes().get("value");
											assertThat(v).isNotNull();
											v.incrementAndGet();
										});
									}
								}), BiConsumer.empty());
							}
							future.join();
							Instant stop = Instant.now();
							Duration concurrentDuration = Duration.between(start, stop);

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

							// Verify integrity of value on other manager
							this.requestSession(manager2, sessionId, session -> {
								assertThat((AtomicInteger) session.getAttributes().get("value")).hasValue(threads * requests * 2);
							});

							// Ensure that concurrent requests complete faster than same number of serial requests
							assertThat(concurrentDuration).isLessThan(serialDuration);

							this.invalidateSession(manager2, sessionId);
						} finally {
							manager2.stop();
						}
					}
				} finally {
					manager1.stop();
				}
			}
		}
	}

	protected void expiration(P parameters) throws Exception {
		Duration expirationDuration = Duration.ofSeconds(120);
		BlockingQueue<ImmutableSession> expiredSessions = new LinkedBlockingQueue<>();
		SessionManagerConfiguration<String> managerConfig1 = new TestSessionManagerConfiguration<>(expiredSessions, DEPLOYMENT_CONTEXT);
		SessionManagerConfiguration<String> managerConfig2 = new TestSessionManagerConfiguration<>(expiredSessions, DEPLOYMENT_CONTEXT);
		try (SessionManagerFactoryProvider<String> provider1 = this.factory.apply(parameters, "member1")) {
			try (SessionManagerFactory<String, AtomicReference<String>> factory1 = provider1.createSessionManagerFactory(SESSION_CONTEXT_FACTORY)) {
				SessionManager<AtomicReference<String>> manager1 = factory1.createSessionManager(managerConfig1);
				manager1.start();
				try (SessionManagerFactoryProvider<String> provider2 = this.factory.apply(parameters, "member2")) {
					try (SessionManagerFactory<String, AtomicReference<String>> factory2 = provider2.createSessionManagerFactory(SESSION_CONTEXT_FACTORY)) {
						SessionManager<AtomicReference<String>> manager2 = factory2.createSessionManager(managerConfig2);
						manager2.start();
						try {
							String sessionId = manager1.getIdentifierFactory().get();
							UUID foo = UUID.randomUUID();
							UUID bar = UUID.randomUUID();

							this.createSession(manager1, sessionId, Map.of("foo", foo, "bar", bar));

							// Setup session to expire soon
							this.requestSession(manager1, sessionId, session -> {
								session.getMetaData().setTimeout(Duration.ofSeconds(2));
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
								assertThat(expiredSession.getMetaData().isNew()).isFalse();
								assertThat(expiredSession.getMetaData().isExpired()).isTrue();
								assertThat(expiredSession.getMetaData().isImmortal()).isFalse();
								this.verifySessionAttributes(expiredSession, Map.of("foo", foo, "bar", bar));

								assertThat(expiredSessions).isEmpty();
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}

							this.verifyNoSession(manager1, sessionId);
							this.verifyNoSession(manager2, sessionId);
						} finally {
							manager2.stop();
						}
					}
				} finally {
					manager1.stop();
				}
			}
		}
	}

	private void createSession(SessionManager<AtomicReference<String>> manager, String sessionId, Map<String, Object> attributes) {
		this.requestSession(manager, manager::createSession, sessionId, session -> {
			assertThat(session.getMetaData().isNew()).isTrue();
			this.verifySessionMetaData(session);
			this.verifySessionAttributes(session, Map.of());
			this.updateSessionAttributes(session, attributes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> new AbstractMap.SimpleEntry<>(null, entry.getValue()))));
			this.verifySessionAttributes(session, attributes);
		});
	}

	private void verifySession(SessionManager<AtomicReference<String>> manager, String sessionId, Map<String, Object> attributes) {
		this.requestSession(manager, sessionId, session -> {
			assertThat(session.getMetaData().isNew()).isFalse();
			this.verifySessionMetaData(session);
			this.verifySessionAttributes(session, attributes);
		});
	}

	private void updateSession(SessionManager<AtomicReference<String>> manager, String sessionId, Map<String, Map.Entry<Object, Object>> attributes) {
		this.requestSession(manager, sessionId, session -> {
			assertThat(session).isNotNull();
			assertThat(session.getId()).isEqualTo(sessionId);
			assertThat(session.getMetaData().isNew()).isFalse();
			this.verifySessionMetaData(session);
			this.updateSessionAttributes(session, attributes);
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
					assertThat(metaData.isNew()).isFalse();
					// Skip these assertions during concurrent session access
					// We would otherwise require memory synchronization to validate
					if (!(Thread.currentThread() instanceof ForkJoinWorkerThread)) {
						// Validate last-access times are within precision bounds
						assertThat(Duration.between(metaData.getLastAccessStartTime(), start).getSeconds()).isEqualTo(0);
						assertThat(Duration.between(metaData.getLastAccessStartTime(), start).truncatedTo(ChronoUnit.MILLIS).getNano()).isEqualTo(0);
						assertThat(Duration.between(end, metaData.getLastAccessEndTime()).getSeconds()).isEqualTo(0);
					}
				}
			}
		}
	}

	private void verifySessionMetaData(Session<AtomicReference<String>> session) {
		assertThat(session.isValid()).isTrue();
		SessionMetaData metaData = session.getMetaData();
		assertThat(metaData.isImmortal()).isFalse();
		assertThat(metaData.isExpired()).isFalse();
		if (metaData.isNew()) {
			assertThat(metaData.getLastAccessStartTime()).isNull();
			assertThat(metaData.getLastAccessEndTime()).isNull();
		} else {
			assertThat(metaData.getLastAccessStartTime()).isNotNull();
			assertThat(metaData.getLastAccessEndTime()).isNotNull();
			// For the request following session creation, the last access time will precede the creation time, but this duration should be tiny
			if (metaData.getLastAccessStartTime().isBefore(metaData.getCreationTime())) {
				assertThat(Duration.between(metaData.getLastAccessStartTime(), metaData.getCreationTime()).getSeconds()).isEqualTo(0);
			}
			assertThat(metaData.getLastAccessStartTime()).isBefore(metaData.getLastAccessEndTime());
		}
	}

	private void verifySessionAttributes(ImmutableSession session, Map<String, Object> attributes) {
		assertThat(session.getAttributes().keySet()).containsAll(attributes.keySet());
		for (Map.Entry<String, Object> entry : attributes.entrySet()) {
			assertThat(session.getAttributes().get(entry.getKey())).isEqualTo(entry.getValue());
		}
	}

	private void updateSessionAttributes(Session<AtomicReference<String>> session, Map<String, Map.Entry<Object, Object>> attributes) {
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
			}
		}
	}

	private static class TestSessionManagerConfiguration<C> implements SessionManagerConfiguration<C> {
		private final Consumer<ImmutableSession> expirationListener;
		private final C context;

		TestSessionManagerConfiguration(BlockingQueue<ImmutableSession> expired, C context) {
			org.wildfly.clustering.function.Consumer<ImmutableSession> queue = expired::add;
			this.expirationListener = queue.map(SimpleImmutableSession::new);
			this.context = context;
		}

		@Override
		public Supplier<String> getIdentifierFactory() {
			org.wildfly.clustering.function.Supplier<UUID> factory = UUID::randomUUID;
			return factory.map(UUID::toString);
		}

		@Override
		public Consumer<ImmutableSession> getExpirationListener() {
			return this.expirationListener;
		}

		@Override
		public Duration getTimeout() {
			return Duration.ofMinutes(1);
		}

		@Override
		public C getContext() {
			return this.context;
		}
	}
}
