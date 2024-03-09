/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache;

import java.util.concurrent.CompletionStage;

/**
 * Removes an entry from a cache.
 * @author Paul Ferraro
 */
public interface Remover<K> {
	/**
	 * Removes the specified entry from the cache.
	 * @param id the cache entry identifier.
	 */
	default void remove(K id) {
		this.removeAsync(id).toCompletableFuture().join();
	}

	/**
	 * Removes the specified entry from the cache.
	 * @param id the cache entry identifier.
	 * @return true, if the entry was removed.
	 */
	CompletionStage<Void> removeAsync(K id);

	/**
	 * Like {@link #remove(Object)}, but does not notify listeners.
	 * @param id the cache entry identifier.
	 */
	default void purge(K id) {
		this.purgeAsync(id).toCompletableFuture().join();
	}

	/**
	 * Removes the specified entry from the cache.
	 * @param id the cache entry identifier.
	 * @return true, if the entry was removed.
	 */
	default CompletionStage<Void> purgeAsync(K id) {
		return this.removeAsync(id);
	}
}
