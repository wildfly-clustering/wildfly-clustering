/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import org.jboss.logging.Logger;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.server.cache.Cache;
import org.wildfly.clustering.server.cache.CacheFactory;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;

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
	private final Function<CacheableSession<C>, Session<C>> identity = Function.identity();

	public CachedSessionManager(SessionManager<C> manager, CacheFactory cacheFactory) {
		super(manager);
		this.sessionCreator = new BiFunction<>() {
			@Override
			public CompletionStage<CacheableSession<C>> apply(String id, Runnable closeTask) {
				return manager.createSessionAsync(id).thenApply(session -> new CachedSession<>(session, closeTask));
			}
		};
		UnaryOperator<Session<C>> validator = new UnaryOperator<>() {
			@Override
			public Session<C> apply(Session<C> session) {
				// If session was invalidated by a concurrent thread, return null instead of an invalid session
				// This will reduce the likelihood that a duplicate invalidation request (e.g. from a double-clicked logout) results in an ISE
				if (session != null && !session.isValid()) {
					try {
						session.close();
						return null;
					} catch (Throwable e) {
						LOGGER.warn(e.getLocalizedMessage(), e);
					}
				}
				return session;
			}
		};
		this.sessionFinder = new BiFunction<>() {
			@Override
			public CompletionStage<CacheableSession<C>> apply(String id, Runnable closeTask) {
				Function<Session<C>, CacheableSession<C>> wrapper = new Function<>() {
					@Override
					public CacheableSession<C> apply(Session<C> session) {
						return (session != null) ? new CachedSession<>(session, closeTask) : null;
					}
				};
				CompletionStage<CacheableSession<C>> result = manager.findSessionAsync(id)
						.thenApply(validator)
						.thenApply(wrapper);
				result.whenComplete(new BiConsumer<>() {
					@Override
					public void accept(CacheableSession<C> session, Throwable e) {
						if (session == null) {
							closeTask.run();
						}
					}
				});
				return result;
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
					LOGGER.warn(e.getLocalizedMessage(), e);
				}
			}
		});
	}

	@Override
	public CompletionStage<Session<C>> createSessionAsync(String id) {
		return this.sessionCache.computeIfAbsent(id, this.sessionCreator).thenApply(this.identity);
	}

	@Override
	public CompletionStage<Session<C>> findSessionAsync(String id) {
		return this.sessionCache.computeIfAbsent(id, this.sessionFinder).thenApply(this.identity);
	}
}
