/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.function.Consumer;

/**
 * Mutator for a cache entry using a compute function.
 * @author Paul Ferraro
 * @param <K> the cache key type
 * @param <V> the cache value type
 */
public class EmbeddedCacheEntryComputer<K, V> implements CacheEntryMutator {

	private final Cache<K, V> cache;
	private final K key;
	private final BiFunction<Object, V, V> function;

	public EmbeddedCacheEntryComputer(Cache<K, V> cache, K key, BiFunction<Object, V, V> function) {
		this.cache = cache;
		this.key = key;
		this.function = function;
	}

	@Override
	public CompletionStage<Void> mutateAsync() {
		// Use FAIL_SILENTLY to prevent mutation from failing locally due to remote exceptions
		return this.cache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES, Flag.FAIL_SILENTLY).computeAsync(this.key, this.function).thenAccept(Consumer.empty());
	}
}
