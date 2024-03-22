/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache;

import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * Locates a pair of values from a cache.
 * @param <I> the identifier type of the cache key
 * @param <K> the key type of the located entry
 * @param <V> the value type of the located entry
 * @author Paul Ferraro
 */
public interface BiCacheEntryLocator<I, K, V> extends CacheEntryLocator<I, Map.Entry<K, V>> {

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

	@Override
	default CompletionStage<Map.Entry<K, V>> tryValueAsync(I id) {
		Map.Entry<CompletionStage<K>, CompletionStage<V>> entry = this.tryEntry(id);
		return entry.getKey().thenCombine(entry.getValue(), (key, value) -> (key != null) && (value != null) ? Map.entry(key, value) : null);
	}
}
