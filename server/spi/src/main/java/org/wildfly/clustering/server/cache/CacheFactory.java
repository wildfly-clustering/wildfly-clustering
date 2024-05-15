/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.cache;

import java.util.function.Consumer;

/**
 * Creates a level-one cache of server-side state.
 * @author Paul Ferraro
 */
public interface CacheFactory {

	/**
	 * Creates a cache that invokes the specified tasks when an entry is added/removed.
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @param addTask the task invoked when a value is added to the cache
	 * @param removeTask the task invoked when a value is removed from the cache
	 * @return a new cache instance
	 */
	<K, V> Cache<K, V> createCache(Consumer<V> addTask, Consumer<V> removeTask);
}
