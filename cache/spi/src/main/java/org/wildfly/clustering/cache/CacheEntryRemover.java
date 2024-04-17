/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache;

import java.util.concurrent.CompletionStage;

/**
 * Removes an entry from a cache.
 * @param <I> the identifier type of the removed value
 * @author Paul Ferraro
 */
public interface CacheEntryRemover<I> {
	/**
	 * Removes the specified entry from the cache.
	 * @param id the cache entry identifier.
	 */
	default void remove(I id) {
		this.removeAsync(id).toCompletableFuture().join();
	}

	/**
	 * Removes the specified entry from the cache.
	 * @param id the cache entry identifier.
	 * @return true, if the entry was removed.
	 */
	CompletionStage<Void> removeAsync(I id);

	/**
	 * Like {@link #remove(Object)}, but does not notify listeners.
	 * @param id the cache entry identifier.
	 */
	default void purge(I id) {
		this.purgeAsync(id).toCompletableFuture().join();
	}

	/**
	 * Removes the specified entry from the cache.
	 * @param id the cache entry identifier.
	 * @return true, if the entry was removed.
	 */
	default CompletionStage<Void> purgeAsync(I id) {
		return this.removeAsync(id);
	}
}
