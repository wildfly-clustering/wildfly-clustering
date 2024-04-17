/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache;

import java.util.concurrent.CompletionStage;

/**
 * Locates a value from a cache.
 * @param <I> the identifier type of the located value
 * @param <V> the located value type
 * @author Paul Ferraro
 */
public interface CacheEntryLocator<I, V> {

	/**
	 * Locates the value in the cache with the specified identifier.
	 * @param id the cache entry identifier
	 * @return the value of the cache entry, or null if not found.
	 */
	default V findValue(I id) {
		return this.findValueAsync(id).toCompletableFuture().join();
	}

	/**
	 * Locates the value in the cache with the specified identifier.
	 * @param id the cache entry identifier
	 * @return the value of the cache entry, or null if not found.
	 */
	CompletionStage<V> findValueAsync(I id);

	/**
	 * Returns the value for the specified key, if possible without contention.
	 * @param id a logical key
	 * @return the value of the cache entry, or null if not found or unavailable.
	 */
	default V tryValue(I id) {
		return this.tryValueAsync(id).toCompletableFuture().join();
	}

	/**
	 * Returns the value for the specified key, if possible without contention.
	 * @param id a logical key
	 * @return the value of the cache entry, or null if not found or unavailable.
	 */
	default CompletionStage<V> tryValueAsync(I id) {
		return this.findValueAsync(id);
	}
}
