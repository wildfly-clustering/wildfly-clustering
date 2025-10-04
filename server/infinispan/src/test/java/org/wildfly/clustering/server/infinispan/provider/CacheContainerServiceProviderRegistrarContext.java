/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.provider;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheType;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.wildfly.clustering.context.AbstractContext;
import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.server.infinispan.CacheContainerGroup;
import org.wildfly.clustering.server.infinispan.EmbeddedCacheManagerGroupContext;

/**
 * Provides a {@link CacheContainerServiceProviderRegistrar} to an integration test.
 * @author Paul Ferraro
 * @param <T> service type
 */
public class CacheContainerServiceProviderRegistrarContext<T> extends AbstractContext<CacheContainerServiceProviderRegistrar<T>> {
	private static final String CACHE_NAME = "registry";

	private final CacheServiceProviderRegistrar<T> registrar;

	public CacheContainerServiceProviderRegistrarContext(String clusterName, String memberName) throws Exception {
		Context<CacheContainerGroup> groupContext = new EmbeddedCacheManagerGroupContext(clusterName, memberName);
		this.accept(groupContext::close);

		EmbeddedCacheManager manager = groupContext.get().getCacheContainer();
		manager.defineConfiguration(CACHE_NAME, new ConfigurationBuilder().clustering().cacheType(CacheType.REPLICATION).build());
		this.accept(() -> manager.undefineConfiguration(CACHE_NAME));

		Cache<?, ?> cache = manager.getCache(CACHE_NAME);
		cache.start();
		this.accept(cache::stop);

		this.registrar = new CacheServiceProviderRegistrar<>(new CacheServiceProviderRegistrar.Configuration() {
			@SuppressWarnings("unchecked")
			@Override
			public <K, V> Cache<K, V> getCache() {
				return (Cache<K, V>) cache;
			}

			@Override
			public CacheContainerGroup getGroup() {
				return groupContext.get();
			}
		});
		this.accept(this.registrar::close);
	}

	@Override
	public CacheContainerServiceProviderRegistrar<T> get() {
		return this.registrar;
	}
}
