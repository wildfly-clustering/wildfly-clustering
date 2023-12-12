/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache;

import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * Creates a pair of entries in a cache.
 * @author Paul Ferraro
 */
public interface BiCreator<I, K, V, C> extends Creator<I, Map.Entry<K, V>, C> {

	/**
	 * Creates a value in the cache, if it does not already exist.
	 * @param id the cache entry identifier.
	 * @parem context the creation context
	 * @return the new value, or the existing value the cache entry already exists.
	 */
	Map.Entry<CompletionStage<K>, CompletionStage<V>> createEntry(I id, C context);

	@Override
	default CompletionStage<Map.Entry<K, V>> createValueAsync(I id, C context) {
		Map.Entry<CompletionStage<K>, CompletionStage<V>> entry = this.createEntry(id, context);
		return entry.getKey().thenCombine(entry.getValue(), Map::entry);
	}
}
