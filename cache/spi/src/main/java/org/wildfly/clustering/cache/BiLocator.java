/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache;

import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * Locates a pair of values from a cache.
 * @author Paul Ferraro
 */
public interface BiLocator<I, K, V> extends Locator<I, Map.Entry<K, V>> {

	/**
	 * Locates the value in the cache with the specified identifier.
	 * @param id the cache entry identifier
	 * @return the value of the cache entry, or null if not found.
	 */
	Map.Entry<CompletionStage<K>, CompletionStage<V>> findEntry(I id);

	/**
	 * Locates the value in the cache with the specified identifier, if available.
	 * @param id the cache entry identifier
	 * @return the value of the cache entry, or null if not found or is in use.
	 */
	default Map.Entry<CompletionStage<K>, CompletionStage<V>> tryEntry(I id) {
		return this.findEntry(id);
	}

	@Override
	default CompletionStage<Map.Entry<K, V>> findValueAsync(I id) {
		Map.Entry<CompletionStage<K>, CompletionStage<V>> entry = this.findEntry(id);
		return entry.getKey().thenCombine(entry.getValue(), (key, value) -> (key != null) && (value != null) ? Map.entry(key, value) : null);
	}
}
