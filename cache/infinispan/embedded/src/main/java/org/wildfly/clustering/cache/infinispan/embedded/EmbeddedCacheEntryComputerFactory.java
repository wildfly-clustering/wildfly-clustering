/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.infinispan.Cache;
import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;

/**
 * Factory that creates compute-based Mutator instances.
 * @author Paul Ferraro
 * @param <K> the cache key type
 * @param <V> the cache value type
 * @param <O> the function operand type
 */
public class EmbeddedCacheEntryComputerFactory<K, V, O> implements CacheEntryMutatorFactory<K, O> {

	private final Cache<K, V> cache;
	private final Function<O, BiFunction<Object, V, V>> functionFactory;

	EmbeddedCacheEntryComputerFactory(Cache<K, V> cache, Function<O, BiFunction<Object, V, V>> functionFactory) {
		this.cache = cache;
		this.functionFactory = functionFactory;
	}

	@Override
	public CacheEntryMutator createMutator(K key, O operand) {
		return new EmbeddedCacheEntryComputer<>(this.cache, key, this.functionFactory.apply(operand));
	}
}
