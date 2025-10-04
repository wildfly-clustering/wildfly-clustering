/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.registry;

import java.util.Map;
import java.util.function.BiFunction;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheType;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.wildfly.clustering.context.AbstractContext;
import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.server.infinispan.CacheContainerGroup;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.infinispan.EmbeddedCacheManagerGroupContext;
import org.wildfly.clustering.server.registry.Registry;
import org.wildfly.clustering.server.registry.RegistryFactory;

/**
 * Provides a {@link Registry} to an integration test.
 * @author Paul Ferraro
 * @param <K> the registry key type
 * @param <V> the registry value type
 */
public class CacheContainerRegistryFactoryContext<K, V> extends AbstractContext<RegistryFactory<CacheContainerGroupMember, K, V>> {
	private static final String CACHE_NAME = "registry";

	private final RegistryFactory<CacheContainerGroupMember, K, V> factory;

	public CacheContainerRegistryFactoryContext(String clusterName, String memberName) throws Exception {
		Context<CacheContainerGroup> groupContext = new EmbeddedCacheManagerGroupContext(clusterName, memberName);
		this.accept(groupContext::close);

		CacheContainerGroup group = groupContext.get();
		EmbeddedCacheManager manager = group.getCacheContainer();
		manager.defineConfiguration(CACHE_NAME, new ConfigurationBuilder().clustering().cacheType(CacheType.REPLICATION).build());
		this.accept(() -> manager.undefineConfiguration(CACHE_NAME));

		Cache<?, ?> cache = manager.getCache(CACHE_NAME);
		cache.start();
		this.accept(cache::stop);

		this.factory = RegistryFactory.singleton(new BiFunction<>() {
			@Override
			public Registry<CacheContainerGroupMember, K, V> apply(Map.Entry<K, V> entry, Runnable closeTask) {
				return new CacheRegistry<>(new CacheRegistry.Configuration<K, V>() {
					@SuppressWarnings("unchecked")
					@Override
					public <KK, VV> Cache<KK, VV> getCache() {
						return (Cache<KK, VV>) cache;
					}

					@Override
					public CacheContainerGroup getGroup() {
						return group;
					}

					@Override
					public Map.Entry<K, V> getEntry() {
						return entry;
					}

					@Override
					public Runnable getCloseTask() {
						return closeTask;
					}
				});
			}
		});
	}

	@Override
	public RegistryFactory<CacheContainerGroupMember, K, V> get() {
		return this.factory;
	}
}
