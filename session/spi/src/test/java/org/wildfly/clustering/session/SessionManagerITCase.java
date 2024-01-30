/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.session.container.ContainerFacadeProvider;
import org.wildfly.common.function.ExceptionBiFunction;

/**
 * Session manager integration test.
 * @author Paul Ferraro
 */
public abstract class SessionManagerITCase<B extends Batch, P extends SessionManagerParameters> {

	private static final String DEPLOYMENT_CONTEXT = "deployment";
	private static final Supplier<AtomicReference<String>> SESSION_CONTEXT_FACTORY = AtomicReference::new;
	private static final ContainerFacadeProvider<Map.Entry<ImmutableSession, String>, String, PassivationListener<String>> CONTAINER_FACADE_PROVIDER = new MockContainerFacadeProvider<>();

	private final ExceptionBiFunction<P, String, SessionManagerFactoryProvider<String, B>, Exception> factory;

	protected SessionManagerITCase(ExceptionBiFunction<P, String, SessionManagerFactoryProvider<String, B>, Exception> factory) {
		this.factory = factory;
	}

	protected void basic(P parameters) throws Exception {
		BlockingQueue<ImmutableSession> expiredSessions = new LinkedBlockingQueue<>();
		SessionManagerConfiguration<String> managerConfig1 = new TestSessionManagerConfiguration<>(expiredSessions, DEPLOYMENT_CONTEXT);
		SessionManagerConfiguration<String> managerConfig2 = new TestSessionManagerConfiguration<>(expiredSessions, DEPLOYMENT_CONTEXT);
		try (SessionManagerFactoryProvider<String, B> provider1 = this.factory.apply(parameters, "member1")) {
			try (SessionManagerFactory<String, AtomicReference<String>, B> factory1 = provider1.createSessionManagerFactory(SESSION_CONTEXT_FACTORY, CONTAINER_FACADE_PROVIDER)) {
				SessionManager<AtomicReference<String>, B> manager1 = factory1.createSessionManager(managerConfig1);
				manager1.start();
				try (SessionManagerFactoryProvider<String, B> provider2 = this.factory.apply(parameters, "member2")) {
					try (SessionManagerFactory<String, AtomicReference<String>, B> factory2 = provider2.createSessionManagerFactory(SESSION_CONTEXT_FACTORY, CONTAINER_FACADE_PROVIDER)) {
						SessionManager<AtomicReference<String>, B> manager2 = factory2.createSessionManager(managerConfig2);
						manager2.start();

						String sessionId = manager1.getIdentifierFactory().get();
						this.verifyNoSession(manager1, sessionId);
						this.verifyNoSession(manager2, sessionId);
						UUID foo = UUID.randomUUID();
						UUID bar = UUID.randomUUID();

						this.createSession(manager1, sessionId, Map.of("foo", foo, "bar", bar));
						this.verifySession(manager1, sessionId, Map.of("foo", foo, "bar", bar));
						this.verifySession(manager2, sessionId, Map.of("foo", foo, "bar", bar));

						UUID baz = UUID.randomUUID();

						this.updateSession(manager1, sessionId, Map.of("foo", new AbstractMap.SimpleEntry<>(foo, null), "bar", Map.entry(bar, baz)));
						this.verifySession(manager1, sessionId, Map.of("bar", baz));
						this.verifySession(manager2, sessionId, Map.of("bar", baz));

						this.invalidateSession(manager1, sessionId);

						this.verifyNoSession(manager1, sessionId);
						this.verifyNoSession(manager2, sessionId);

						assertTrue(expiredSessions.isEmpty());

						manager2.stop();
					}
				}
				manager1.stop();
			}
		}
	}

	protected void expiration(P parameters) throws Exception {
		BlockingQueue<ImmutableSession> expiredSessions = new LinkedBlockingQueue<>();
		SessionManagerConfiguration<String> managerConfig1 = new TestSessionManagerConfiguration<>(expiredSessions, DEPLOYMENT_CONTEXT);
		SessionManagerConfiguration<String> managerConfig2 = new TestSessionManagerConfiguration<>(expiredSessions, DEPLOYMENT_CONTEXT);
		try (SessionManagerFactoryProvider<String, B> provider1 = this.factory.apply(parameters, "member1")) {
			try (SessionManagerFactory<String, AtomicReference<String>, B> factory1 = provider1.createSessionManagerFactory(SESSION_CONTEXT_FACTORY, CONTAINER_FACADE_PROVIDER)) {
				SessionManager<AtomicReference<String>, B> manager1 = factory1.createSessionManager(managerConfig1);
				manager1.start();
				try (SessionManagerFactoryProvider<String, B> provider2 = this.factory.apply(parameters, "member2")) {
					try (SessionManagerFactory<String, AtomicReference<String>, B> factory2 = provider2.createSessionManagerFactory(SESSION_CONTEXT_FACTORY, CONTAINER_FACADE_PROVIDER)) {
						SessionManager<AtomicReference<String>, B> manager2 = factory2.createSessionManager(managerConfig2);
						manager2.start();

						String sessionId = manager1.getIdentifierFactory().get();
						this.verifyNoSession(manager1, sessionId);
						this.verifyNoSession(manager2, sessionId);
						UUID foo = UUID.randomUUID();
						UUID bar = UUID.randomUUID();

						this.createSession(manager1, sessionId, Map.of("foo", foo, "bar", bar));

						this.verifySession(manager2, sessionId, Map.of("foo", foo, "bar", bar));

						this.requestSession(manager2, sessionId, session -> {
							session.getMetaData().setTimeout(Duration.ofMillis(1));
						});

						try {
							ImmutableSession expiredSession = expiredSessions.poll(60, TimeUnit.SECONDS);
							assertEquals(sessionId, expiredSession.getId());
							assertFalse(expiredSession.isValid());
							assertFalse(expiredSession.getMetaData().isNew());
							assertTrue(expiredSession.getMetaData().isExpired());
							assertFalse(expiredSession.getMetaData().isImmortal());
							this.verifySessionAttributes(expiredSession, Map.of("foo", foo, "bar", bar));

							assertEquals(0, expiredSessions.size(), expiredSessions.toString());
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}

						this.verifyNoSession(manager1, sessionId);
						this.verifyNoSession(manager2, sessionId);

						manager2.stop();
					}
				}
				manager1.stop();
			}
		}
	}

	private void createSession(SessionManager<AtomicReference<String>, B> manager, String sessionId, Map<String, Object> attributes) {
		this.requestSession(manager, manager::createSession, sessionId, session -> {
			assertTrue(session.getMetaData().isNew());
			this.verifySessionMetaData(session);
			this.verifySessionAttributes(session, Map.of());
			this.updateSessionAttributes(session, attributes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> new AbstractMap.SimpleEntry<>(null, entry.getValue()))));
			this.verifySessionAttributes(session, attributes);
		});
	}

	private void verifySession(SessionManager<AtomicReference<String>, B> manager, String sessionId, Map<String, Object> attributes) {
		this.requestSession(manager, sessionId, session -> {
			assertFalse(session.getMetaData().isNew());
			this.verifySessionMetaData(session);
			this.verifySessionAttributes(session, attributes);
		});
	}

	private void updateSession(SessionManager<AtomicReference<String>, B> manager, String sessionId, Map<String, Map.Entry<Object, Object>> attributes) {
		this.requestSession(manager, sessionId, session -> {
			assertNotNull(session);
			assertEquals(sessionId, session.getId());
			assertFalse(session.getMetaData().isNew());
			this.verifySessionMetaData(session);
			this.updateSessionAttributes(session, attributes);
		});
	}

	private void invalidateSession(SessionManager<AtomicReference<String>, B> manager, String sessionId) {
		this.requestSession(manager, sessionId, session -> {
			session.invalidate();
			assertFalse(session.isValid());
			assertThrows(IllegalStateException.class, () -> session.getAttributes());
			assertThrows(IllegalStateException.class, () -> session.getMetaData());
		});
	}

	private void requestSession(SessionManager<AtomicReference<String>, B> manager, String sessionId, Consumer<Session<AtomicReference<String>>> action) {
		this.requestSession(manager, manager::findSession, sessionId, action);
	}

	private void requestSession(SessionManager<AtomicReference<String>, B> manager, Function<String, Session<AtomicReference<String>>> sessionFactory, String sessionId, Consumer<Session<AtomicReference<String>>> action) {
		Instant start = Instant.now();
		try (B batch = manager.getBatcher().createBatch()) {
			try (Session<AtomicReference<String>> session = sessionFactory.apply(sessionId)) {
				assertNotNull(session);
				assertEquals(sessionId, session.getId());
				action.accept(session);
				if (session.isValid()) {
					session.getMetaData().setLastAccess(start, Instant.now());
				}
			}
		}
	}

	private void verifySessionContext(Session<AtomicReference<String>> session, String previousContext, String currentContext) {
		assertEquals(previousContext, session.getContext().getAndSet(currentContext));
	}

	private void verifySessionMetaData(Session<AtomicReference<String>> session) {
		assertTrue(session.isValid());
		assertFalse(session.getMetaData().isImmortal());
		assertFalse(session.getMetaData().isExpired());
	}

	private void verifySessionAttributes(ImmutableSession session, Map<String, Object> attributes) {
		assertEquals(attributes.keySet(), session.getAttributes().getAttributeNames());
		for (Map.Entry<String, Object> entry : attributes.entrySet()) {
			assertEquals(entry.getValue(), session.getAttributes().getAttribute(entry.getKey()));
		}
	}

	private void updateSessionAttributes(Session<AtomicReference<String>> session, Map<String, Map.Entry<Object, Object>> attributes) {
		for (Map.Entry<String, Map.Entry<Object, Object>> entry : attributes.entrySet()) {
			Map.Entry<Object, Object> values = entry.getValue();
			assertEquals(values.getKey(), session.getAttributes().setAttribute(entry.getKey(), values.getValue()));
		}
	}

	private void removeSessionAttributes(Session<AtomicReference<String>> session, Map<String, Object> attributes) {
		for (Map.Entry<String, Object> entry : attributes.entrySet()) {
			assertEquals(entry.getValue(), session.getAttributes().removeAttribute(entry.getKey()));
		}
	}

	private void verifyNoSession(SessionManager<AtomicReference<String>, B> manager, String sessionId) {
		try (B batch = manager.getBatcher().createBatch()) {
			try (Session<AtomicReference<String>> session = manager.findSession(sessionId)) {
				assertNull(session);
			}
		}
	}

	private static class TestSessionManagerConfiguration<DC> implements SessionManagerConfiguration<DC> {
		private final BlockingQueue<ImmutableSession> expired;
		private final DC context;

		TestSessionManagerConfiguration(BlockingQueue<ImmutableSession> expired, DC context) {
			this.expired = expired;
			this.context = context;
		}

		@Override
		public Supplier<String> getIdentifierFactory() {
			return () -> UUID.randomUUID().toString();
		}

		@Override
		public Consumer<ImmutableSession> getExpirationListener() {
			return this.expired::add;
		}

		@Override
		public Duration getTimeout() {
			return Duration.ofMinutes(1);
		}

		@Override
		public DC getContext() {
			return this.context;
		}
	}
}
