/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.batch.SuspendedBatch;
import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.function.BiFunction;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Runner;
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
	private final Supplier<Batch> batchFactory;
	private final BiFunction<String, Instant, CompletionStage<Session<C>>> sessionCreator;
	private final BiFunction<String, Runnable, CompletionStage<CacheableSession<C>>> defaultSessionCreator;
	private final BiFunction<String, Runnable, CompletionStage<CacheableSession<C>>> sessionFinder;
	private final UnaryOperator<Session<C>> validator = UnaryOperator.when(Session::isValid, UnaryOperator.identity(), UnaryOperator.of(Session::close, Supplier.of(null)));

	/**
	 * Creates a cached session manager decorator.
	 * @param manager a session manager
	 * @param cacheFactory a cache factory
	 */
	public CachedSessionManager(SessionManager<C> manager, CacheFactory cacheFactory) {
		super(manager);
		this.batchFactory = manager.getBatchFactory();
		this.sessionCreator = manager::createSessionAsync;
		// If completed exceptionally, return an invalid session that rethrows this exception on Session.close()
		// If completed with null, return an invalid session that we can filter later
		this.defaultSessionCreator = new SessionManagerFunction<>(this.batchFactory, manager::createSessionAsync);
		this.sessionFinder = new SessionManagerFunction<>(this.batchFactory, manager::findSessionAsync);
		this.sessionCache = cacheFactory.createCache(Consumer.of(), new Consumer<CompletionStage<CacheableSession<C>>>() {
			@Override
			public void accept(CompletionStage<CacheableSession<C>> stage) {
				try {
					CacheableSession<C> session = stage.toCompletableFuture().join();
					if (session != null) {
						try (Batch batch = session.resume()) {
							Optional.ofNullable(session.get()).ifPresent(Consumer.close());
						}
					}
				} catch (CompletionException | CancellationException e) {
					// This would already have been handled
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
		return this.sessionCache.computeIfAbsent(id, new SessionManagerFunction<>(this.batchFactory, this.sessionCreator.composeUnary(UnaryOperator.identity(), Function.of(creationTime)))).thenApply(this.validator);
	}

	@Override
	public CompletionStage<Session<C>> findSessionAsync(String id) {
		return this.sessionCache.computeIfAbsent(id, this.sessionFinder).thenApply(this.validator);
	}

	Set<String> keySet() {
		return this.sessionCache.keySet();
	}

	static class SessionManagerFunction<C> implements BiFunction<String, Runnable, CompletionStage<CacheableSession<C>>>, Session<C> {
		private final Supplier<Batch> batchFactory;
		private final Function<String, CompletionStage<Session<C>>> operation;

		SessionManagerFunction(Supplier<Batch> batchFactory, Function<String, CompletionStage<Session<C>>> operation) {
			this.batchFactory = batchFactory;
			this.operation = operation;
		}

		@Override
		public CompletionStage<CacheableSession<C>> apply(String id, Runnable closeTask) {
			SuspendedBatch suspended = this.batchFactory.get().suspend();
			try (Context<Batch> context = suspended.resumeWithContext()) {
				return this.operation.apply(id).handle(new BiFunction<>() {
					@Override
					public CacheableSession<C> apply(Session<C> session, Throwable exception) {
						Runnable onClose = (exception != null) ? Runner.of(List.of(closeTask, Runner.of(Supplier.of(exception).thenThrow(CompletionException::new), Consumer.of()))) : closeTask;
						return new CachedSession<>((session != null) ? session : SessionManagerFunction.this, suspended, onClose);
					}
				});
			}
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
