/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.function.BiFunction;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.function.UnaryOperator;
import org.wildfly.clustering.server.cache.Cache;
import org.wildfly.clustering.server.cache.CacheFactory;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionMetaData;

/**
 * A session manager decorator that shares session references across concurrent threads.
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public class CachedSessionManager<C> extends DecoratedSessionManager<C> {
	static final System.Logger LOGGER = System.getLogger(CachedSessionManager.class.getName());

	private final Cache<String, CompletionStage<CacheableSession<C>>> sessionCache;
	private final BiFunction<String, Instant, CompletionStage<Session<C>>> sessionCreator;
	private final BiFunction<String, Runnable, CompletionStage<CacheableSession<C>>> defaultSessionCreator;
	private final BiFunction<String, Runnable, CompletionStage<CacheableSession<C>>> sessionFinder;
	private final UnaryOperator<Session<C>> validator = new UnaryOperator<>() {
		@Override
		public Session<C> apply(Session<C> session) {
			if (!session.isValid()) {
				session.close();
				return null;
			}
			return session;
		}
	};

	/**
	 * Creates a cached session manager decorator.
	 * @param manager a session manager
	 * @param cacheFactory a cache factory
	 */
	public CachedSessionManager(SessionManager<C> manager, CacheFactory cacheFactory) {
		super(manager);
		this.sessionCreator = manager::createSessionAsync;
		// If completed exceptionally, return an invalid session that rethrows this exception on Session.close()
		// If completed with null, return an invalid session that we can filter later
		this.defaultSessionCreator = new SessionManagerFunction<>(manager::createSessionAsync);
		this.sessionFinder = new SessionManagerFunction<>(manager::findSessionAsync);
		this.sessionCache = cacheFactory.createCache(Consumer.of(), new Consumer<CompletionStage<CacheableSession<C>>>() {
			@Override
			public void accept(CompletionStage<CacheableSession<C>> stage) {
				try {
					Optional.ofNullable(stage.toCompletableFuture().join()).map(CacheableSession::get).ifPresent(Session::close);
				} catch (CompletionException | CancellationException e) {
					LOGGER.log(System.Logger.Level.DEBUG, e.getLocalizedMessage(), e);
				}
			}
		});
	}

	@Override
	public CompletionStage<Session<C>> createSessionAsync(String id) {
		return this.sessionCache.computeIfAbsent(id, this.defaultSessionCreator).thenApply(this.validator);
	}

	@Override
	public CompletionStage<Session<C>> createSessionAsync(String id, Instant creationTime) {
		return this.sessionCache.computeIfAbsent(id, new SessionManagerFunction<>(this.sessionCreator.composeUnary(UnaryOperator.identity(), Function.of(creationTime)))).thenApply(this.validator);
	}

	@Override
	public CompletionStage<Session<C>> findSessionAsync(String id) {
		return this.sessionCache.computeIfAbsent(id, this.sessionFinder).thenApply(this.validator);
	}

	Set<String> keySet() {
		return this.sessionCache.keySet();
	}

	static class SessionManagerFunction<C> implements BiFunction<String, Runnable, CompletionStage<CacheableSession<C>>>, Session<C> {
		private final Function<String, CompletionStage<Session<C>>> operation;

		SessionManagerFunction(Function<String, CompletionStage<Session<C>>> operation) {
			this.operation = operation;
		}

		@Override
		public CompletionStage<CacheableSession<C>> apply(String id, Runnable closeTask) {
			return this.operation.apply(id).handle((session, exception) -> new CachedSession<>((session != null) ? session : this, (exception != null) ? Supplier.of(closeTask, Supplier.of(exception).thenThrow(CompletionException::new)).thenAccept(Consumer.of()) : closeTask));
		}

		@Override
		public String getId() {
			return null;
		}

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public Map<String, Object> getAttributes() {
			return Map.of();
		}

		@Override
		public SessionMetaData getMetaData() {
			return null;
		}

		@Override
		public void invalidate() {
		}

		@Override
		public C getContext() {
			return null;
		}

		@Override
		public void close() {
		}
	}
}
