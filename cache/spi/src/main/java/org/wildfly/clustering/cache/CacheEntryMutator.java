/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import org.wildfly.clustering.function.Runnable;

/**
 * Updates a cache entry within the cache.
 * @author Paul Ferraro
 */
public interface CacheEntryMutator extends java.lang.Runnable {
	System.Logger LOGGER = System.getLogger(CacheEntryMutator.class.getName());

	@Override
	default void run() {
		try {
			this.runAsync().toCompletableFuture().join();
		} catch (CompletionException | CancellationException e) {
			LOGGER.log(System.Logger.Level.ERROR, e.getLocalizedMessage(), e);
		}
	}

	CompletionStage<Void> runAsync();

	CacheEntryMutator withMaxIdle(Supplier<Duration> maxIdle);

	/**
	 * Trivial {@link CacheEntryMutator} implementation that does nothing.
	 * New cache entries created within the context of a batch, in particular, do not require mutation.
	 */
	CacheEntryMutator EMPTY = new CacheEntryMutator() {
		private final CompletionStage<Void> completed = CompletableFuture.completedStage(null);

		@Override
		public CompletionStage<Void> runAsync() {
			return this.completed;
		}

		@Override
		public CacheEntryMutator withMaxIdle(Supplier<Duration> maxIdle) {
			return this;
		}
	};

	static CacheEntryMutator of(Iterable<CacheEntryMutator> mutators) {
		return new CacheEntryMutator() {
			@Override
			public CompletionStage<Void> runAsync() {
				CompletionStage<Void> result = CompletableFuture.completedStage(null);
				for (CacheEntryMutator mutator : mutators) {
					result = result.runAfterBoth(mutator.runAsync(), Runnable.empty());
				}
				return result;
			}

			@Override
			public CacheEntryMutator withMaxIdle(Supplier<Duration> maxIdle) {
				for (CacheEntryMutator mutator : mutators) {
					mutator.withMaxIdle(maxIdle);
				}
				return this;
			}
		};
	}
}
