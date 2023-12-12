/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import jakarta.transaction.TransactionManager;

import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.manager.EmbeddedCacheManager;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.infinispan.BasicCacheConfiguration;

/**
 * @author Paul Ferraro
 */
public interface EmbeddedCacheConfiguration extends EmbeddedCacheContainerConfiguration, BasicCacheConfiguration {

	@Override
	<K, V> Cache<K, V> getCache();

	@Override
	default TransactionManager getTransactionManager() {
		return this.getCache().getAdvancedCache().getTransactionManager();
	}

	@Override
	default EmbeddedCacheManager getCacheContainer() {
		return this.getCache().getCacheManager();
	}

	@Override
	default CacheProperties getCacheProperties() {
		return new EmbeddedCacheProperties(this.getCache().getCacheConfiguration());
	}

	/**
	 * Returns a cache with select-for-update semantics.
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @return a cache with select-for-update semantics.
	 */
	default <K, V> Cache<K, V> getReadForUpdateCache() {
		return this.getCacheProperties().isLockOnRead() ? this.<K, V>getCache().getAdvancedCache().withFlags(Flag.FORCE_WRITE_LOCK) : this.getCache();
	}

	/**
	 * Returns a cache with try-lock write semantic, e.g. whose write operations will return null if another transaction owns the write lock.
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @return a cache with try-lock semantics.
	 */
	default <K, V> Cache<K, V> getTryLockCache() {
		return this.getCacheProperties().isLockOnWrite() ? this.<K, V>getCache().getAdvancedCache().withFlags(Flag.ZERO_LOCK_ACQUISITION_TIMEOUT, Flag.FAIL_SILENTLY) : this.getCache();
	}

	/**
	 * Returns a cache with select-for-update and try-lock semantics.
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @return a cache with try-lock and select-for-update semantics.
	 */
	default <K, V> Cache<K, V> getTryReadForUpdateCache() {
		return this.getCacheProperties().isLockOnRead() ? this.<K, V>getCache().getAdvancedCache().withFlags(Flag.FORCE_WRITE_LOCK, Flag.ZERO_LOCK_ACQUISITION_TIMEOUT, Flag.FAIL_SILENTLY) : this.getCache();
	}

	/**
	 * Returns a cache for use with write-only operations, e.g. put/remove where previous values are not needed.
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @return a cache for use with write-only operations.
	 */
	default <K, V> Cache<K, V> getWriteOnlyCache() {
		return this.<K, V>getCache().getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
	}

	/**
	 * Returns a cache whose write operations do not trigger cache listeners.
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @return a cache whose write operations do not trigger cache listeners.
	 */
	default <K, V> Cache<K, V> getSilentWriteCache() {
		return this.<K, V>getCache().getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES, Flag.SKIP_LISTENER_NOTIFICATION);
	}
}
