/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan;

import jakarta.transaction.TransactionManager;

import org.infinispan.commons.CacheException;
import org.infinispan.commons.api.BasicCache;
import org.wildfly.clustering.cache.CacheConfiguration;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.batch.Batcher;
import org.wildfly.clustering.cache.infinispan.batch.TransactionBatch;
import org.wildfly.clustering.cache.infinispan.batch.TransactionalBatcher;

/**
 * @author Paul Ferraro
 */
public interface BasicCacheConfiguration extends CacheConfiguration<TransactionBatch>, BasicCacheContainerConfiguration {

	<K, V> BasicCache<K, V> getCache();

	TransactionManager getTransactionManager();

	CacheProperties getCacheProperties();

	default Batcher<TransactionBatch> getBatcher() {
		return new TransactionalBatcher<>(this.getCache().getName(), this.getTransactionManager(), CacheException::new);
	}
}
