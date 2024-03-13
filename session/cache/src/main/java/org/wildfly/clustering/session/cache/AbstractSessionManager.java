/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.jboss.logging.Logger;
import org.wildfly.clustering.cache.CacheConfiguration;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.batch.Batcher;
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
 * @param <B> the batch type
 * @author Paul Ferraro
 */
public abstract class AbstractSessionManager<C, MV, AV, SC, B extends Batch> implements SessionManager<SC, B>, SessionStatistics {
	protected final Logger logger = Logger.getLogger(this.getClass());

	private final SessionFactory<C, MV, AV, SC> factory;
	private final Consumer<ImmutableSession> expirationListener;
	private final Expiration expiration;
	private final Supplier<String> identifierFactory;
	private final C context;
	private final Batcher<B> batcher;
	private final UnaryOperator<Session<SC>> wrapper;

	protected AbstractSessionManager(SessionManagerConfiguration<C> configuration, CacheConfiguration<B> cacheConfiguration, SessionFactory<C, MV, AV, SC> factory, Consumer<ImmutableSession> sessionCloseTask) {
		this.identifierFactory = configuration.getIdentifierFactory();
		this.context = configuration.getContext();
		this.batcher = cacheConfiguration.getBatcher();
		this.expiration = configuration;
		this.expirationListener = configuration.getExpirationListener();
		this.factory = factory;
		this.wrapper = session -> new ManagedSession<>(this, session, sessionCloseTask);
	}

	@Override
	public Supplier<String> getIdentifierFactory() {
		return this.identifierFactory;
	}

	@Override
	public Batcher<B> getBatcher() {
		return this.batcher;
	}

	@Override
	public CompletionStage<Session<SC>> createSessionAsync(String id) {
		this.logger.tracef("Creating session %s", id);
		return this.factory.createValueAsync(id, this.expiration.getTimeout()).thenApply(entry -> this.wrapper.apply(this.factory.createSession(id, entry, this.context)));
	}

	@Override
	public CompletionStage<Session<SC>> findSessionAsync(String id) {
		this.logger.tracef("Locating session %s", id);
		return this.factory.findValueAsync(id).thenApply(entry -> {
			if (entry == null) {
				this.logger.tracef("Session %s not found", id);
				return null;
			}
			ImmutableSession session = this.factory.createImmutableSession(id, entry);
			if (session.getMetaData().isExpired()) {
				this.logger.tracef("Session %s was found, but has expired", id);
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
	public SessionStatistics getStatistics() {
		return this;
	}
}
