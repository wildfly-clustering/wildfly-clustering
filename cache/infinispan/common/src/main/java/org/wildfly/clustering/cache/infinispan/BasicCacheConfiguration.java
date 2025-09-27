/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan;

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
 * @author Paul Ferraro
 */
public interface BasicCacheConfiguration extends CacheConfiguration, BasicCacheContainerConfiguration {

	<K, V> BasicCache<K, V> getCache();

	<K, V> CacheEntryMutatorFactory<K, V> getCacheEntryMutatorFactory();

	<K, V, O> CacheEntryMutatorFactory<K, O> getCacheEntryMutatorFactory(Function<O, BiFunction<Object, V, V>> functionFactory);

	Optional<TransactionManager> getTransactionManager();

	@Override
	default Supplier<Batch> getBatchFactory() {
		return this.getTransactionManager().<Supplier<Batch>>map(tm -> new TransactionalBatchFactory(this.getName(), tm, CacheException::new)).orElseGet(() -> new SimpleContextualBatchFactory(this.getName()));
	}
}
