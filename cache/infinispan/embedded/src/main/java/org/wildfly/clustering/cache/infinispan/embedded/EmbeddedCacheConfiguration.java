/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import jakarta.transaction.TransactionManager;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;

import org.infinispan.Cache;
import org.infinispan.commons.CacheException;
import org.infinispan.context.Flag;
import org.infinispan.manager.EmbeddedCacheManager;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.infinispan.BasicCacheConfiguration;

/**
 * @author Paul Ferraro
 */
public interface EmbeddedCacheConfiguration extends EmbeddedCacheContainerConfiguration, BasicCacheConfiguration {

	@Override
	<K, V> Cache<K, V> getCache();

	@Override
	default String getName() {
		return Map.of("container", EmbeddedCacheContainerConfiguration.super.getName(), "cache", this.getCache().getName()).toString();
	}

	@Override
	default boolean isActive() {
		return this.getCache().getStatus().allowInvocations() && (this.getCache().getCacheConfiguration().clustering().hash().capacityFactor() > Float.MIN_VALUE);
	}

	@Override
	default <K, V> CacheEntryMutatorFactory<K, V> getCacheEntryMutatorFactory() {
		return new EmbeddedCacheEntryMutatorFactory<>(this.getWriteCache());
	}

	@Override
	default <K, V, O> CacheEntryMutatorFactory<K, O> getCacheEntryMutatorFactory(Function<O, BiFunction<Object, V, V>> functionFactory) {
		return new EmbeddedCacheEntryComputerFactory<>(this.getWriteCache(), functionFactory);
	}

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
		return new EmbeddedCacheProperties(this.getCache());
	}

	/**
	 * Returns a cache for use with write operations, e.g. put/compute/replace.
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @return a cache with write semantics.
	 */
	default <K, V> Cache<K, V> getWriteCache() {
		return this.getCache();
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
		return this.<K, V>getWriteCache().getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
	}

	/**
	 * Returns a cache whose write operations do not trigger cache listeners.
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @return a cache whose write operations do not trigger cache listeners.
	 */
	default <K, V> Cache<K, V> getSilentWriteCache() {
		return this.<K, V>getWriteCache().getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES, Flag.SKIP_LISTENER_NOTIFICATION);
	}

	/**
	 * Returns a retry configuration suitable for operations on this cache.
	 * @return a retry configuration
	 */
	default RetryConfig getRetryConfig() {
		Cache<?, ?> cache = this.getCache();
		long timeout = cache.getCacheConfiguration().locking().lockAcquisitionTimeout();
		int attempts = 1;
		// Calculate the number of attempts
		for (long interval = timeout; interval > 1; interval /= 10) {
			attempts += 1;
		}
		return RetryConfig.custom()
				.maxAttempts(attempts)
				.failAfterMaxAttempts(true)
				.intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofMillis(1), 10.0))
				.retryExceptions(CacheException.class, IOException.class, UncheckedIOException.class)
				.build();
	}
}
