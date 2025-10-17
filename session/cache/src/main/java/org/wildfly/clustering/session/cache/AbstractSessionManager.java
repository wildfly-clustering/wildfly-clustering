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
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionManagerConfiguration;
import org.wildfly.clustering.session.SessionStatistics;

/**
 * An abstract {@link SessionManager} implementation that delegates most implementation details to a {@link SessionFactory}.
 * @param <DC> the deployment context type
 * @param <MV> the session metadata value type
 * @param <AV> the session attribute value type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public abstract class AbstractSessionManager<DC, MV, AV, SC> implements SessionManager<SC>, SessionStatistics {
	/** The logger for this session manager */
	protected final System.Logger logger = System.getLogger(this.getClass().getName());

	private final SessionFactory<DC, MV, AV, SC> sessionFactory;
	private final BiFunction<String, SC, Session<SC>> detachedSessionFactory;
	private final Consumer<ImmutableSession> expiredSessionHandler;
	private final Expiration expiration;
	private final IdentifierFactoryService<String> identifierFactory;
	private final DC context;
	private final Supplier<Batch> batchFactory;
	private final UnaryOperator<Session<SC>> wrapper;

	/**
	 * Configuration of a session manager.
	 * @param <DC> the deployment context type
	 * @param <MV> the session metadata value type
	 * @param <AV> the session attribute value type
	 * @param <SC> the session context type
	 */
	protected interface Configuration<DC, MV, AV, SC> extends SessionManagerConfiguration<DC> {
		@Override
		IdentifierFactoryService<String> getIdentifierFactory();

		/**
		 * Returns the configuration associated with a cache.
		 * @return the configuration associated with a cache.
		 */
		CacheConfiguration getCacheConfiguration();

		/**
		 * Returns a factory for creating a session.
		 * @return a factory for creating a session.
		 */
		SessionFactory<DC, MV, AV, SC> getSessionFactory();

		/**
		 * Returns a factory for creating a detached session.
		 * @return a factory for creating a detached session.
		 */
		BiFunction<String, SC, Session<SC>> getDetachedSessionFactory();

		/**
		 * Returns a task to invoke on session close.
		 * @return a task to invoke on session close.
		 */
		Consumer<ImmutableSession> getExpiredSessionHandler();

		/**
		 * Returns a task to invoke on session close.
		 * @return a task to invoke on session close.
		 */
		Consumer<ImmutableSession> getSessionCloseTask();
	}

	/**
	 * Creates a session manager using the specified configuration.
	 * @param configuration the configuration of the session manager
	 */
	protected AbstractSessionManager(Configuration<DC, MV, AV, SC> configuration) {
		this.identifierFactory = configuration.getIdentifierFactory();
		this.context = configuration.getContext();
		this.batchFactory = configuration.getCacheConfiguration().getBatchFactory();
		this.expiration = configuration;
		this.expiredSessionHandler = configuration.getExpiredSessionHandler();
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
			ImmutableSessionMetaData metaData = this.sessionFactory.getSessionMetaDataFactory().createImmutableSessionMetaData(id, entry.getKey());
			if (metaData.isExpired()) {
				this.logger.log(System.Logger.Level.TRACE, "Session {0} was found, but has expired: {1}", id, metaData);
				this.expiredSessionHandler.accept(this.sessionFactory.createImmutableSession(id, metaData, this.sessionFactory.getSessionAttributesFactory().createImmutableSessionAttributes(id, entry.getValue())));
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
		return this.detachedSessionFactory.apply(id, this.sessionFactory.getSessionContextFactory().get());
	}

	@Override
	public SessionStatistics getStatistics() {
		return this;
	}
}
