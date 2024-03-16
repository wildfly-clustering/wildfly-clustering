/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.globalstate;

import java.util.EnumSet;
import java.util.concurrent.CompletionStage;

import org.infinispan.Cache;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.util.DependencyGraph;

/**
 * Custom {@link org.infinispan.globalstate.impl.VolatileLocalConfigurationStorage} that doesn't mess with the {@link org.infinispan.eviction.impl.PassivationManager} or {@link org.infinispan.persistence.manager.PersistenceManager}.
 * @author Paul Ferraro
 */
public class LocalConfigurationStorage extends org.infinispan.globalstate.impl.VolatileLocalConfigurationStorage {

	@Override
	public CompletionStage<Void> removeCache(String name, EnumSet<CacheContainerAdmin.AdminFlag> flags) {
		return this.blockingManager.<Void>supplyBlocking(() -> {
			Cache<?, ?> cache = this.cacheManager.getCache(name, false);
			if (cache != null) {
				cache.stop();
			}
			GlobalComponentRegistry globalComponentRegistry = GlobalComponentRegistry.of(this.cacheManager);
			globalComponentRegistry.removeCache(name);
			// Remove cache configuration and remove it from the computed cache name list
			this.configurationManager.removeConfiguration(name);
			// Remove cache from dependency graph
			GlobalComponentRegistry.componentOf(this.cacheManager, DependencyGraph.class).remove(name);
			return null;
		}, name);
	}
}
