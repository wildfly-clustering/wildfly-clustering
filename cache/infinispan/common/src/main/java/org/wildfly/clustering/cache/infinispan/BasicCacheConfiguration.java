/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan;

import java.time.Duration;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import jakarta.transaction.TransactionManager;

import org.infinispan.commons.CacheException;
import org.infinispan.commons.api.BasicCache;
import org.wildfly.clustering.cache.CacheConfiguration;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.infinispan.batch.SimpleContextualBatchFactory;
import org.wildfly.clustering.cache.infinispan.batch.TransactionalBatchFactory;
import org.wildfly.clustering.function.Supplier;

/**
 * Infinispan cache configuration specialization for a {@link BasicCache}.
 * @author Paul Ferraro
 */
public interface BasicCacheConfiguration extends CacheConfiguration, BasicCacheContainerConfiguration {

	@Override
	default String getName() {
		return this.getCache().getName();
	}

	/**
	 * Returns the cache associated with this configuration.
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @return the cache associated with this configuration.
	 */
	<K, V> BasicCache<K, V> getCache();

	/**
	 * Returns a cache configured for write operations that return/require previous values.
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @return a cache configured for read/write operations.
	 */
	default <K, V> BasicCache<K, V> getReadWriteCache() {
		return this.getCache();
	}

	/**
	 * Returns a cache configured for write-only operations, i.e. that do not return/require previous values.
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @return a cache configured for write-only operations.
	 */
	default <K, V> BasicCache<K, V> getWriteOnlyCache() {
		return this.getCache();
	}

	/**
	 * Returns the cache entry mutator associated with this configuration.
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @return the cache entry mutator associated with this configuration.
	 */
	<K, V> CacheEntryMutatorFactory<K, V> getCacheEntryMutatorFactory();

	/**
	 * Returns the compute-based cache entry mutator associated with this configuration.
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @param <O> the operand type
	 * @param functionFactory a compute function factory
	 * @return the compute-based cache entry mutator associated with this configuration.
	 */
	<K, V, O> CacheEntryMutatorFactory<K, O> getCacheEntryMutatorFactory(Function<O, BiFunction<Object, V, V>> functionFactory);

	/**
	 * If present, returns the transaction manager associated with this cache configuration.
	 * @return and optional transaction manager
	 */
	Optional<TransactionManager> getTransactionManager();

	/**
	 * Returns the duration of time a service should wait for ongoing operations to complete prior to stopping.
	 * @return the duration of time a service should wait for ongoing operations to complete prior to stopping.
	 */
	Duration getStopTimeout();

	@Override
	default Supplier<Batch> getBatchFactory() {
		return this.getTransactionManager().<Supplier<Batch>>map(tm -> new TransactionalBatchFactory(this.getName(), tm, CacheException::new)).orElseGet(() -> new SimpleContextualBatchFactory(this.getName()));
	}
}
