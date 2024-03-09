/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Updates a cache entry within the cache.
 * @author Paul Ferraro
 */
public interface CacheEntryMutator {
	/**
	 * Ensure that this object replicates.
	 */
	default void mutate() {
		this.mutateAsync().toCompletableFuture().join();
	}

	CompletionStage<Void> mutateAsync();

	/**
	 * Trivial {@link CacheEntryMutator} implementation that does nothing.
	 * New cache entries created within the context of a batch, in particular, do not require mutation.
	 */
	CacheEntryMutator NO_OP = new CacheEntryMutator() {
		@Override
		public CompletionStage<Void> mutateAsync() {
			return CompletableFuture.completedStage(null);
		}
	};
}
