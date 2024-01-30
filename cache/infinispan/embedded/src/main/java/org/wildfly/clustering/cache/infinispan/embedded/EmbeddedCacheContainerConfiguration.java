/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import java.util.concurrent.Executor;

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
	default Executor getExecutor() {
		return this.getBlockingManager().asExecutor(this.getClass().getSimpleName());
	}

	default BlockingManager getBlockingManager() {
		return this.getCacheContainer().getGlobalComponentRegistry().getComponent(BlockingManager.class);
	}
}
