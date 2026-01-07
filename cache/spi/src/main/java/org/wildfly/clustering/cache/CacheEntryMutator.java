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

import org.wildfly.clustering.function.Runner;

/**
 * Updates a cache entry within the cache.
 * @author Paul Ferraro
 */
public interface CacheEntryMutator extends Runner {
	/** The mutator logger */
	System.Logger LOGGER = System.getLogger(CacheEntryMutator.class.getName());

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

	@Override
	default void run() {
		try {
			this.runAsync().toCompletableFuture().join();
		} catch (CompletionException | CancellationException e) {
			LOGGER.log(System.Logger.Level.ERROR, e.getLocalizedMessage(), e);
		}
	}

	/**
	 * Mutates the associated cache entry asynchronously.
	 * @return a stage that completes when the cache entry mutation completes.
	 */
	CompletionStage<Void> runAsync();

	/**
	 * Applies a maximum idle duration to the mutated cache entry.
	 * @param maxIdle a provider of a maximum idle duration
	 * @return a reference to this mutator.
	 */
	CacheEntryMutator withMaxIdle(Supplier<Duration> maxIdle);

	/**
	 * Returns a composite mutator that runs the specified mutators.
	 * @param mutators a number of mutators
	 * @return a composite mutator.
	 */
	static CacheEntryMutator of(Iterable<CacheEntryMutator> mutators) {
		return new CacheEntryMutator() {
			@Override
			public CompletionStage<Void> runAsync() {
				CompletionStage<Void> result = CompletableFuture.completedStage(null);
				for (CacheEntryMutator mutator : mutators) {
					result = result.runAfterBoth(mutator.runAsync(), Runner.of());
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
