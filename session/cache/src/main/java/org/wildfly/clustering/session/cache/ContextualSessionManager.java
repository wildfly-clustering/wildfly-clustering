/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.server.context.Context;
import org.wildfly.clustering.server.context.ContextFactory;
import org.wildfly.clustering.server.context.Contextual;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionStatistics;
import org.wildfly.common.function.Functions;

/**
 * A concurrent session manager, that can share session references across concurrent threads.
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public class ContextualSessionManager<C> implements SessionManager<C> {

	private final SessionManager<C> manager;
	private final Context<String, CompletionStage<ContextualSession<C>>> sessionContext;
	private final BiFunction<String, Runnable, CompletionStage<ContextualSession<C>>> sessionCreator;
	private final BiFunction<String, Runnable, CompletionStage<ContextualSession<C>>> sessionFinder;
	private final UnaryOperator<Session<C>> validator = new UnaryOperator<>() {
		@Override
		public Session<C> apply(Session<C> session) {
			// If session was invalidated by a concurrent thread, return null instead of an invalid session
			// This will reduce the likelihood that a duplicate invalidation request (e.g. from a double-clicked logout) results in an ISE
			if (session != null && !session.isValid()) {
				session.close();
				return null;
			}
			return session;
		}
	};

	public ContextualSessionManager(SessionManager<C> manager, ContextFactory contextFactory) {
		this.manager = manager;
		this.sessionCreator = new BiFunction<>() {
			@Override
			public CompletionStage<ContextualSession<C>> apply(String id, Runnable closeTask) {
				return manager.createSessionAsync(id).thenApply(session -> new ContextualSessionRegistration<>(session, closeTask));
			}
		};
		Function<Runnable, ContextualSession<C>> empty = closeTask -> {
			closeTask.run();
			return null;
		};
		this.sessionFinder = new BiFunction<>() {
			@Override
			public CompletionStage<ContextualSession<C>> apply(String id, Runnable closeTask) {
				return manager.findSessionAsync(id).thenApply(session -> (session != null) ? new ContextualSessionRegistration<>(session, closeTask) : empty.apply(closeTask));
			}
		};
		this.sessionContext = contextFactory.createContext(Functions.discardingConsumer(), new Consumer<CompletionStage<ContextualSession<C>>>() {
			@Override
			public void accept(CompletionStage<ContextualSession<C>> future) {
				future.thenAccept(session -> Optional.ofNullable(session).ifPresent(Contextual::end));
			}
		});
	}

	@Override
	public CompletionStage<Session<C>> createSessionAsync(String id) {
		return this.sessionContext.computeIfAbsent(id, this.sessionCreator).thenApply(Function.identity());
	}

	@Override
	public CompletionStage<Session<C>> findSessionAsync(String id) {
		return this.sessionContext.computeIfAbsent(id, this.sessionFinder).thenApply(this.validator);
	}

	@Override
	public CompletionStage<ImmutableSession> findImmutableSessionAsync(String id) {
		return this.manager.findImmutableSessionAsync(id);
	}

	@Override
	public Session<C> getDetachedSession(String id) {
		return this.manager.getDetachedSession(id);
	}

	@Override
	public Supplier<String> getIdentifierFactory() {
		return this.manager.getIdentifierFactory();
	}

	@Override
	public void start() {
		this.manager.start();
	}

	@Override
	public void stop() {
		this.manager.stop();
	}

	@Override
	public Supplier<Batch> getBatchFactory() {
		return this.manager.getBatchFactory();
	}

	@Override
	public SessionStatistics getStatistics() {
		return this.manager.getStatistics();
	}
}
