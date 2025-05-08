/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.wildfly.clustering.cache.CacheConfiguration;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.server.expiration.Expiration;
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

	private final Supplier<SessionManager<SC>> manager;
	private final SessionFactory<C, MV, AV, SC> factory;
	private final Consumer<ImmutableSession> expirationListener;
	private final Expiration expiration;
	private final Supplier<String> identifierFactory;
	private final C context;
	private final Supplier<Batch> batchFactory;
	private final UnaryOperator<Session<SC>> wrapper;

	protected AbstractSessionManager(Supplier<SessionManager<SC>> manager, SessionManagerConfiguration<C> configuration, CacheConfiguration cacheConfiguration, SessionFactory<C, MV, AV, SC> factory, Consumer<ImmutableSession> sessionCloseTask) {
		this.manager = manager;
		this.identifierFactory = configuration.getIdentifierFactory();
		this.context = configuration.getContext();
		this.batchFactory = cacheConfiguration.getBatchFactory();
		this.expiration = configuration;
		this.expirationListener = configuration.getExpirationListener();
		this.factory = factory;
		this.wrapper = new UnaryOperator<>() {
			@Override
			public Session<SC> apply(Session<SC> session) {
				return new ManagedSession<>(new AttachedSession<>(session, sessionCloseTask), new DetachedSession<>(manager.get(), session.getId(), session.getContext()));
			}
		};
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
		return this.factory.createValueAsync(id, this.expiration.getTimeout()).thenApply(entry -> this.wrapper.apply(this.factory.createSession(id, entry, this.context)));
	}

	@Override
	public CompletionStage<Session<SC>> findSessionAsync(String id) {
		this.logger.log(System.Logger.Level.TRACE, "Locating session {0}", id);
		return this.factory.findValueAsync(id).thenApply(entry -> {
			if (entry == null) {
				this.logger.log(System.Logger.Level.TRACE, "Session {0} not found", id);
				return null;
			}
			ImmutableSession session = this.factory.createImmutableSession(id, entry);
			if (session.getMetaData().isExpired()) {
				this.logger.log(System.Logger.Level.TRACE, "Session {0} was found, but has expired", id);
				this.expirationListener.accept(session);
				this.factory.removeAsync(id);
				return null;
			}
			return this.wrapper.apply(this.factory.createSession(id, entry, this.context));
		});
	}

	@Override
	public CompletionStage<ImmutableSession> findImmutableSessionAsync(String id) {
		return this.factory.findValueAsync(id).thenApply(entry -> (entry != null) ? new SimpleImmutableSession(this.factory.createImmutableSession(id, entry)) : null);
	}

	@Override
	public Session<SC> getDetachedSession(String id) {
		return new DetachedSession<>(this.manager.get(), id, this.factory.getContextFactory().get());
	}

	@Override
	public SessionStatistics getStatistics() {
		return this;
	}
}
