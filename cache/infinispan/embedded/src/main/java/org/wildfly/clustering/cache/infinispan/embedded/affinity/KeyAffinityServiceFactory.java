/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache.infinispan.embedded.affinity;

import java.util.function.Predicate;

import org.infinispan.Cache;
import org.infinispan.affinity.KeyAffinityService;
import org.infinispan.affinity.KeyGenerator;
import org.infinispan.configuration.cache.Configurations;
import org.infinispan.remoting.transport.Address;

/**
 * Factory for creating a key affinity service.
 * @author Paul Ferraro
 */
public interface KeyAffinityServiceFactory {
	/**
	 * Creates a key affinity service for use with the specified cache, that generates local keys using the specified generator.
	 * @param <K> the cache key type
	 * @param cache a cache for which keys should be generated
	 * @param generator a generator of cache keys
	 * @return a key affinity service
	 */
	@SuppressWarnings("resource")
	default <K> KeyAffinityService<K> createService(Cache<? extends K, ?> cache, KeyGenerator<K> generator) {
		return this.createService(cache, generator, cache.getCacheManager().getAddress()::equals);
	}

	/**
	 * Creates a key affinity service for use with the specified cache, that generates key for members matching the specified filter, using the specified generator.
	 * @param <K> the cache key type
	 * @param cache a cache for which keys should be generated
	 * @param generator a generator of cache keys
	 * @param filter a filter restricting the addresses for which the service should generate keys
	 * @return a key affinity service
	 */
	<K> KeyAffinityService<K> createService(Cache<? extends K, ?> cache, KeyGenerator<K> generator, Predicate<Address> filter);

	/** Singleton instance */
	KeyAffinityServiceFactory INSTANCE = new KeyAffinityServiceFactory() {
		@Override
		public <K> KeyAffinityService<K> createService(Cache<? extends K, ?> cache, KeyGenerator<K> generator, Predicate<Address> filter) {
			return Configurations.needSegments(cache.getCacheConfiguration()) ? new DefaultKeyAffinityService<>(cache, generator, filter) : new SimpleKeyAffinityService<>(generator);
		}
	};
}
