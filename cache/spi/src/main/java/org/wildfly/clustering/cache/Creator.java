/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache;

import java.util.concurrent.CompletionStage;

/**
 * Creates an entry in a cache.
 * @author Paul Ferraro
 */
public interface Creator<K, V, C> {

	/**
	 * Creates a value in the cache, if it does not already exist.
	 * @param id the cache entry identifier.
	 * @parem context the creation context
	 * @return the new value, or the existing value the cache entry already exists.
	 */
	default V createValue(K id, C context) {
		return this.createValueAsync(id, context).toCompletableFuture().join();
	}

	/**
	 * Creates a value in the cache, if it does not already exist.
	 * @param id the cache entry identifier.
	 * @parem context the creation context
	 * @return the new value, or the existing value the cache entry already exists.
	 */
	CompletionStage<V> createValueAsync(K id, C context);
}
