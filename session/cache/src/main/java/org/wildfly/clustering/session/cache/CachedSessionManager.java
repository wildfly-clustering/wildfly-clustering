/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.jboss.logging.Logger;
import org.wildfly.clustering.server.cache.Cache;
import org.wildfly.clustering.server.cache.CacheFactory;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionMetaData;
import org.wildfly.common.function.Functions;

/**
 * A concurrent session manager, that can share session references across concurrent threads.
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public class CachedSessionManager<C> extends DecoratedSessionManager<C> {
	static final Logger LOGGER = Logger.getLogger(CachedSessionManager.class);

	private final Cache<String, CompletionStage<CacheableSession<C>>> sessionCache;
	private final BiFunction<String, Runnable, CompletionStage<CacheableSession<C>>> sessionCreator;
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
		// If completed exceptionally, return an invalid session that rethrows this exception on Session.close()
		// If completed with null, return an invalid session that we can filter later
		this.sessionCreator = new SessionManagerFunction<>(manager::createSessionAsync);
		this.sessionFinder = new SessionManagerFunction<>(manager::findSessionAsync);
		this.sessionCache = cacheFactory.createCache(Functions.discardingConsumer(), new Consumer<CompletionStage<CacheableSession<C>>>() {
			@Override
			public void accept(CompletionStage<CacheableSession<C>> stage) {
				try {
					Optional.ofNullable(stage.toCompletableFuture().join()).map(CacheableSession::get).ifPresent(Session::close);
				} catch (CompletionException | CancellationException e) {
					LOGGER.debug(e.getLocalizedMessage(), e);
				}
			}
		});
	}

	@Override
	public CompletionStage<Session<C>> createSessionAsync(String id) {
		return this.sessionCache.computeIfAbsent(id, this.sessionCreator).thenApply(this.validator);
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
			return this.operation.apply(id).handle((session, exception) -> new CachedSession<>((session != null) ? session : this, (exception != null) ? new Runnable() {
				@Override
				public void run() {
					closeTask.run();
					throw new CompletionException(exception);
				}
			} : closeTask));
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
