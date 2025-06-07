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
public interface CacheEntryMutator {
	System.Logger LOGGER = System.getLogger(CacheEntryMutator.class.getPackageName());

	/**
	 * Ensure that this object replicates.
	 */
	default void mutate() {
		try {
			this.mutateAsync().toCompletableFuture().join();
		} catch (CompletionException | CancellationException e) {
			LOGGER.log(System.Logger.Level.ERROR, e.getLocalizedMessage(), e);
		}
	}

	CompletionStage<Void> mutateAsync();

	CacheEntryMutator withMaxIdle(Supplier<Duration> maxIdle);

	/**
	 * Trivial {@link CacheEntryMutator} implementation that does nothing.
	 * New cache entries created within the context of a batch, in particular, do not require mutation.
	 */
	CacheEntryMutator EMPTY = new CacheEntryMutator() {
		private final CompletionStage<Void> completed = CompletableFuture.completedStage(null);

		@Override
		public CompletionStage<Void> mutateAsync() {
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
			public CompletionStage<Void> mutateAsync() {
				CompletionStage<Void> result = CompletableFuture.completedStage(null);
				for (CacheEntryMutator mutator : mutators) {
					result = result.runAfterBoth(mutator.mutateAsync(), Runnable.empty());
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
