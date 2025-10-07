/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.Map;

/**
 * A scheduler for a cache entry.
 * @author Paul Ferraro
 * @param <K> the cache key type
 * @param <V> the cache value type
 */
public interface CacheEntryScheduler<K, V> {
	/**
	 * Schedules the specified cache entry.
	 * @param entry a cache entry
	 */
	void scheduleEntry(Map.Entry<K, V> entry);

	/**
	 * Cancels the entry with the specified key.
	 * @param key a cache key
	 */
	void cancelKey(K key);

	/**
	 * Indicates whether or not the entry with the specified cache key is scheduled.
	 * @param key a cache key
	 * @return true, if the entry with the specified cache key is scheduled, false otherwise.
	 */
	boolean containsKey(K key);
}
