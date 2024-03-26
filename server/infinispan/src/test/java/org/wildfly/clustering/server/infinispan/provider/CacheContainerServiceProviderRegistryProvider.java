/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.provider;

import java.util.function.Supplier;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.wildfly.clustering.server.AutoCloseableProvider;
import org.wildfly.clustering.server.infinispan.CacheContainerGroup;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupProvider;
import org.wildfly.clustering.server.infinispan.EmbeddedCacheManagerGroupProvider;

/**
 * Provides a {@link CacheContainerServiceProviderRegistry} to an integration test.
 * @author Paul Ferraro
 * @param <T> service type
 */
public class CacheContainerServiceProviderRegistryProvider<T> extends AutoCloseableProvider implements Supplier<CacheContainerServiceProviderRegistry<T>> {
	private static final String CACHE_NAME = "registry";

	private final CacheServiceProviderRegistry<T> registry;

	public CacheContainerServiceProviderRegistryProvider(String clusterName, String memberName) throws Exception {
		CacheContainerGroupProvider provider = new EmbeddedCacheManagerGroupProvider(clusterName, memberName);
		this.accept(provider::close);

		EmbeddedCacheManager manager = provider.getCacheManager();
		manager.defineConfiguration(CACHE_NAME, new ConfigurationBuilder().clustering().cacheMode(CacheMode.REPL_SYNC).build());
		this.accept(() -> manager.undefineConfiguration(CACHE_NAME));

		Cache<?, ?> cache = manager.getCache(CACHE_NAME);
		cache.start();
		this.accept(cache::stop);

		this.registry = new CacheServiceProviderRegistry<>(new CacheServiceProviderRegistryConfiguration() {
			@SuppressWarnings("unchecked")
			@Override
			public <K, V> Cache<K, V> getCache() {
				return (Cache<K, V>) cache;
			}

			@Override
			public Object getId() {
				return CACHE_NAME;
			}

			@Override
			public CacheContainerGroup getGroup() {
				return provider.getGroup();
			}
		});
		this.accept(this.registry::close);
	}

	@Override
	public CacheContainerServiceProviderRegistry<T> get() {
		return this.registry;
	}
}
