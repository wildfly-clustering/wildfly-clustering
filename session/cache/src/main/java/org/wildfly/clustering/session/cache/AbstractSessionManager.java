/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.wildfly.clustering.cache.CacheConfiguration;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.server.expiration.Expiration;
import org.wildfly.clustering.server.manager.IdentifierFactoryService;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionManagerConfiguration;
import org.wildfly.clustering.session.SessionStatistics;

/**
 * An abstract {@link SessionManager} implementation that delegates most implementation details to a {@link SessionFactory}.
 * @param <C> the session manager context type
 * @param <MV> the session metadata value type
 * @param <AV> the session attribute value type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public abstract class AbstractSessionManager<C, MV, AV, SC> implements SessionManager<SC>, SessionStatistics {
	protected final System.Logger logger = System.getLogger(this.getClass().getName());

	private final SessionFactory<C, MV, AV, SC> sessionFactory;
	private final BiFunction<String, SC, Session<SC>> detachedSessionFactory;
	private final Consumer<ImmutableSession> expirationListener;
	private final Expiration expiration;
	private final IdentifierFactoryService<String> identifierFactory;
	private final C context;
	private final Supplier<Batch> batchFactory;
	private final UnaryOperator<Session<SC>> wrapper;

	protected interface Configuration<C, MV, AV, SC> extends SessionManagerConfiguration<C> {
		@Override
		IdentifierFactoryService<String> getIdentifierFactory();
		CacheConfiguration getCacheConfiguration();
		SessionFactory<C, MV, AV, SC> getSessionFactory();
		BiFunction<String, SC, Session<SC>> getDetachedSessionFactory();
		Consumer<ImmutableSession> getSessionCloseTask();
	}

	protected AbstractSessionManager(Configuration<C, MV, AV, SC> configuration) {
		this.identifierFactory = configuration.getIdentifierFactory();
		this.context = configuration.getContext();
		this.batchFactory = configuration.getCacheConfiguration().getBatchFactory();
		this.expiration = configuration;
		this.expirationListener = configuration.getExpirationListener();
		this.sessionFactory = configuration.getSessionFactory();
		this.detachedSessionFactory = configuration.getDetachedSessionFactory();
		this.wrapper = new UnaryOperator<>() {
			@Override
			public Session<SC> apply(Session<SC> session) {
				return new ManagedSession<>(new AttachedSession<>(session, configuration.getSessionCloseTask()), configuration.getDetachedSessionFactory().apply(session.getId(), session.getContext()));
			}
		};
	}

	@Override
	public boolean isStarted() {
		return this.identifierFactory.isStarted();
	}

	@Override
	public void start() {
		this.identifierFactory.start();
	}

	@Override
	public void stop() {
		this.identifierFactory.stop();
	}

	@Override
	public Supplier<String> getIdentifierFactory() {
		return this.identifierFactory;
	}

	@Override
	public Supplier<Batch> getBatchFactory() {
		return this.batchFactory;
	}

	@Override
	public CompletionStage<Session<SC>> createSessionAsync(String id) {
		this.logger.log(System.Logger.Level.TRACE, "Creating session {0}", id);
		return this.sessionFactory.createValueAsync(id, this.expiration.getTimeout()).thenApply(entry -> this.wrapper.apply(this.sessionFactory.createSession(id, entry, this.context)));
	}

	@Override
	public CompletionStage<Session<SC>> findSessionAsync(String id) {
		this.logger.log(System.Logger.Level.TRACE, "Locating session {0}", id);
		return this.sessionFactory.findValueAsync(id).thenApply(entry -> {
			if (entry == null) {
				this.logger.log(System.Logger.Level.TRACE, "Session {0} not found", id);
				return null;
			}
			ImmutableSession session = this.sessionFactory.createImmutableSession(id, entry);
			if (session.getMetaData().isExpired()) {
				this.logger.log(System.Logger.Level.TRACE, "Session {0} was found, but has expired", id);
				this.expirationListener.accept(session);
				this.sessionFactory.removeAsync(id);
				return null;
			}
			return this.wrapper.apply(this.sessionFactory.createSession(id, entry, this.context));
		});
	}

	@Override
	public CompletionStage<ImmutableSession> findImmutableSessionAsync(String id) {
		return this.sessionFactory.findValueAsync(id).thenApply(entry -> (entry != null) ? new SimpleImmutableSession(this.sessionFactory.createImmutableSession(id, entry)) : null);
	}

	@Override
	public Session<SC> getDetachedSession(String id) {
		return this.detachedSessionFactory.apply(id, this.sessionFactory.getContextFactory().get());
	}

	@Override
	public SessionStatistics getStatistics() {
		return this;
	}
}
