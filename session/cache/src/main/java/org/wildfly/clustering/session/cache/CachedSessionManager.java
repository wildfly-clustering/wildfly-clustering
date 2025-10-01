/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Supplier;
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
	private final BiFunction<String, Runnable, CompletionStage<CacheableSession<C>>> sessionCreator;
	private final BiFunction<String, Runnable, CompletionStage<CacheableSession<C>>> sessionFinder;
	private final UnaryOperator<Session<C>> validator = new UnaryOperator<>() {
		@Override
		public Session<C> apply(Session<C> session) {
			if (!session.isValid()) {
				try {
					session.close();
					return null;
				} catch (Throwable e) {
					LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
				}
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
		this.sessionCreator = new BiFunction<>() {
			@Override
			public CompletionStage<CacheableSession<C>> apply(String id, Runnable closeTask) {
				return manager.createSessionAsync(id).thenApply(session -> new CachedSession<>(session, closeTask));
			}
		};
		// Placeholder for a missing session
		Session<C> missingSession = new Session<>() {
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
		};
		this.sessionFinder = new BiFunction<>() {
			@Override
			public CompletionStage<CacheableSession<C>> apply(String id, Runnable closeTask) {
				Function<Session<C>, CacheableSession<C>> wrapper = new Function<>() {
					@Override
					public CacheableSession<C> apply(Session<C> session) {
						return new CachedSession<>(session, closeTask);
					}
				};
				// If session not found, use placeholder
				return manager.findSessionAsync(id).thenApply(wrapper.withDefault(Objects::nonNull, Supplier.of(missingSession)));
			}
		};
		this.sessionCache = cacheFactory.createCache(Consumer.empty(), new Consumer<CompletionStage<CacheableSession<C>>>() {
			@Override
			public void accept(CompletionStage<CacheableSession<C>> future) {
				try {
					CacheableSession<C> session = future.toCompletableFuture().join();
					if (session != null) {
						session.get().close();
					}
				} catch (CancellationException e) {
					// Ignore
				} catch (Throwable e) {
					LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
				}
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
