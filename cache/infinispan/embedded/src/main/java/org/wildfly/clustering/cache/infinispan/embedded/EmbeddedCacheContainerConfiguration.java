/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.infinispan.commons.IllegalLifecycleStateException;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.util.concurrent.BlockingManager;
import org.wildfly.clustering.cache.infinispan.BasicCacheContainerConfiguration;

/**
 * @author Paul Ferraro
 */
public interface EmbeddedCacheContainerConfiguration extends BasicCacheContainerConfiguration {

	@Override
	EmbeddedCacheManager getCacheContainer();

	@Override
	default String getName() {
		return this.getCacheContainer().getCacheManagerConfiguration().cacheManagerName();
	}

	@Override
	default Executor getExecutor() {
		Executor executor = GlobalComponentRegistry.componentOf(this.getCacheContainer(), BlockingManager.class).asExecutor(this.getClass().getSimpleName());
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
}
