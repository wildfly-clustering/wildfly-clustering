/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;

import jakarta.transaction.TransactionManager;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.infinispan.commons.IllegalLifecycleStateException;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.infinispan.BasicCacheConfiguration;

/**
 * Configuration identifying a remote cache.
 * @author Paul Ferraro
 */
public interface RemoteCacheConfiguration extends RemoteCacheContainerConfiguration, BasicCacheConfiguration {

	@Override
	<K, V> RemoteCache<K, V> getCache();

	@Override
	default boolean isActive() {
		return this.getCache().getRemoteCacheContainer().isStarted();
	}

	@Override
	default String getName() {
		return Map.of("container", RemoteCacheContainerConfiguration.super.getName(), "cache", this.getCache().getName()).toString();
	}

	@Override
	default <K, V> CacheEntryMutatorFactory<K, V> getCacheEntryMutatorFactory() {
		return new RemoteCacheEntryMutatorFactory<>(this.getCache());
	}

	@Override
	default <K, V, O> CacheEntryMutatorFactory<K, O> getCacheEntryMutatorFactory(Function<O, BiFunction<Object, V, V>> functionFactory) {
		return new RemoteCacheEntryComputerFactory<>(this.getCache(), functionFactory);
	}

	@Override
	default RemoteCacheContainer getCacheContainer() {
		return this.getCache().getRemoteCacheContainer();
	}

	@Override
	default Executor getExecutor() {
		@SuppressWarnings("removal")
		Executor executor = this.getCache().getRemoteCacheManager().getAsyncExecutorService();
		return new Executor() {
			@Override
			public void execute(Runnable command) {
				try {
					executor.execute(command);
				} catch (IllegalLifecycleStateException e) {
					throw new RejectedExecutionException(e);
				}
			}
		};
	}

	@Override
	default TransactionManager getTransactionManager() {
		return this.getCache().getTransactionManager();
	}

	/**
	 * Returns a cache with select-for-update semantics.
	 * @param <K> the cache key type
	 * @param <V> the cache value type
	 * @return a cache with select-for-update semantics.
	 */
	default <K, V> RemoteCache<K, V> getReadForUpdateCache() {
		RemoteCache<K, V> cache = this.getCache();
		return this.getCacheProperties().isLockOnRead() ? new ReadForUpdateRemoteCache<>(cache) : cache;
	}

	default <K, V> RemoteCache<K, V> getIgnoreReturnCache() {
		RemoteCache<K, V> cache = this.getCache();
		return this.getNearCacheMode().enabled() ? cache : cache.withFlags(Flag.SKIP_LISTENER_NOTIFICATION);
	}

	default <K, V> RemoteCache<K, V> getForceReturnCache() {
		RemoteCache<K, V> cache = this.getCache();
		return this.getNearCacheMode().enabled() ? cache.withFlags(Flag.FORCE_RETURN_VALUE) : cache.withFlags(Flag.FORCE_RETURN_VALUE, Flag.SKIP_LISTENER_NOTIFICATION);
	}

	default NearCacheMode getNearCacheMode() {
		RemoteCache<?, ?> cache = this.getCache();
		return cache.getRemoteCacheContainer().getConfiguration().remoteCaches().get(cache.getName()).nearCacheMode();
	}

	@Override
	default CacheProperties getCacheProperties() {
		return new RemoteCacheProperties(this.getCache());
	}
}
