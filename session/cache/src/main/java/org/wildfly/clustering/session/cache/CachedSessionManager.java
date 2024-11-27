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

import org.wildfly.clustering.server.cache.Cache;
import org.wildfly.clustering.server.cache.CacheFactory;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.common.function.Functions;

/**
 * A concurrent session manager, that can share session references across concurrent threads.
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public class CachedSessionManager<C> extends DecoratedSessionManager<C> {

	private final Cache<String, CompletionStage<CacheableSession<C>>> sessionCache;
	private final BiFunction<String, Runnable, CompletionStage<CacheableSession<C>>> sessionCreator;
	private final BiFunction<String, Runnable, CompletionStage<CacheableSession<C>>> sessionFinder;
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

	public CachedSessionManager(SessionManager<C> manager, CacheFactory cacheFactory) {
		super(manager);
		this.sessionCreator = new BiFunction<>() {
			@Override
			public CompletionStage<CacheableSession<C>> apply(String id, Runnable closeTask) {
				return manager.createSessionAsync(id).thenApply(session -> new CachedSession<>(session, closeTask));
			}
		};
		Function<Runnable, CacheableSession<C>> empty = closeTask -> {
			closeTask.run();
			return null;
		};
		this.sessionFinder = new BiFunction<>() {
			@Override
			public CompletionStage<CacheableSession<C>> apply(String id, Runnable closeTask) {
				return manager.findSessionAsync(id).thenApply(session -> (session != null) ? new CachedSession<>(session, closeTask) : empty.apply(closeTask));
			}
		};
		this.sessionCache = cacheFactory.createCache(Functions.discardingConsumer(), new Consumer<CompletionStage<CacheableSession<C>>>() {
			@Override
			public void accept(CompletionStage<CacheableSession<C>> future) {
				future.thenAccept(session -> Optional.ofNullable(session).map(Supplier::get).ifPresent(Session::close));
			}
		});
	}

	@Override
	public CompletionStage<Session<C>> createSessionAsync(String id) {
		return this.sessionCache.computeIfAbsent(id, this.sessionCreator).thenApply(Function.identity());
	}

	@Override
	public CompletionStage<Session<C>> findSessionAsync(String id) {
		return this.sessionCache.computeIfAbsent(id, this.sessionFinder).thenApply(this.validator);
	}
}
