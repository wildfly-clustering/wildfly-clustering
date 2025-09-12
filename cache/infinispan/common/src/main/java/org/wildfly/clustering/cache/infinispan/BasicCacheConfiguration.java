/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan;

import java.util.function.Supplier;

import jakarta.transaction.TransactionManager;

import org.infinispan.commons.CacheException;
import org.infinispan.commons.api.BasicCache;
import org.wildfly.clustering.cache.CacheConfiguration;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.infinispan.batch.TransactionBatch;

/**
 * @author Paul Ferraro
 */
public interface BasicCacheConfiguration extends CacheConfiguration, BasicCacheContainerConfiguration {

	<K, V> BasicCache<K, V> getCache();

	TransactionManager getTransactionManager();

	@Override
	default Supplier<Batch> getBatchFactory() {
		TransactionManager tm = this.getTransactionManager();
		return (tm != null) ? TransactionBatch.factory(this.getCache().getName(), tm, CacheException::new) : Batch.factory();
	}
}
