/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache;

import java.util.concurrent.CompletionStage;

/**
 * Locates a value from a cache.
 * @author Paul Ferraro
 */
public interface Locator<K, V> {

	/**
	 * Locates the value in the cache with the specified identifier.
	 * @param id the cache entry identifier
	 * @return the value of the cache entry, or null if not found.
	 */
	default V findValue(K id) {
		return this.findValueAsync(id).toCompletableFuture().join();
	}

	/**
	 * Locates the value in the cache with the specified identifier.
	 * @param id the cache entry identifier
	 * @return the value of the cache entry, or null if not found.
	 */
	CompletionStage<V> findValueAsync(K id);

	/**
	 * Returns the value for the specified key, if possible without contention.
	 * @param id a logical key
	 * @return the value of the cache entry, or null if not found or unavailable.
	 */
	default V tryValue(K id) {
		return this.tryValueAsync(id).toCompletableFuture().join();
	}

	/**
	 * Returns the value for the specified key, if possible without contention.
	 * @param id a logical key
	 * @return the value of the cache entry, or null if not found or unavailable.
	 */
	default CompletionStage<V> tryValueAsync(K id) {
		return this.findValueAsync(id);
	}
}
