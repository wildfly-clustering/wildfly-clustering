/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.util.concurrent.Executor;

import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.wildfly.clustering.cache.infinispan.BasicCacheContainerConfiguration;

/**
 * A configuration with an associated remote cache container.
 * @author Paul Ferraro
 */
public interface RemoteCacheContainerConfiguration extends BasicCacheContainerConfiguration {
	/**
	 * Creates a configuration for the specified container.
	 * @param container a cache container
	 * @return a configuration for the specified container.
	 */
	static RemoteCacheContainerConfiguration of(RemoteCacheManager container) {
		return new RemoteCacheContainerConfiguration() {
			@Override
			public RemoteCacheContainer getCacheContainer() {
				return container;
			}

			@Override
			public Executor getExecutor() {
				return container.getAsyncExecutorService();
			}
		};
	}

	@Override
	RemoteCacheContainer getCacheContainer();

	@Override
	default String getName() {
		return this.getCacheContainer().getConfiguration().statistics().jmxName();
	}
}
