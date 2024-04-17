/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache;

import java.util.concurrent.CompletionStage;

/**
 * Creates an entry in a cache.
 * @param <I> the logical identifier of the created value
 * @param <V> the created value type
 * @param <C> the context of the created type
 * @author Paul Ferraro
 */
public interface CacheEntryCreator<I, V, C> {

	/**
	 * Creates a value in the cache, if it does not already exist.
	 * @param id the cache entry identifier.
	 * @param context the context of the created value
	 * @return the new value, or the existing value the cache entry already exists.
	 */
	default V createValue(I id, C context) {
		return this.createValueAsync(id, context).toCompletableFuture().join();
	}

	/**
	 * Creates a value in the cache, if it does not already exist.
	 * @param id the cache entry identifier.
	 * @param context the context of the created value
	 * @return the new value, or the existing value the cache entry already exists.
	 */
	CompletionStage<V> createValueAsync(I id, C context);
}
