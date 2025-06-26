/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.dispatcher;

import org.infinispan.manager.EmbeddedCacheManager;
import org.jgroups.Address;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheManagerContext;
import org.wildfly.clustering.context.AbstractContext;
import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.server.group.GroupCommandDispatcherFactory;
import org.wildfly.clustering.server.jgroups.ChannelGroupMember;
import org.wildfly.clustering.server.jgroups.dispatcher.ChannelCommandDispatcherFactory;
import org.wildfly.clustering.server.jgroups.dispatcher.ChannelCommandDispatcherFactoryContext;

/**
 * @author Paul Ferraro
 */
public class CacheContainerCommandDispatcherFactoryContext extends AbstractContext<CacheContainerCommandDispatcherFactory> {

	private final CacheContainerCommandDispatcherFactory factory;

	public CacheContainerCommandDispatcherFactoryContext(String clusterName, String memberName) {
		try {
			Context<ChannelCommandDispatcherFactory> factory = new ChannelCommandDispatcherFactoryContext(clusterName, memberName);
			this.accept(factory::close);
			Context<EmbeddedCacheManager> manager = new EmbeddedCacheManagerContext(factory.get().getGroup().getChannel());
			this.accept(manager::close);
			this.factory = new EmbeddedCacheManagerCommandDispatcherFactory<>(new ChannelEmbeddedCacheManagerCommandDispatcherFactoryConfiguration() {
				@Override
				public EmbeddedCacheManager getCacheContainer() {
					return manager.get();
				}

				@Override
				public GroupCommandDispatcherFactory<Address, ChannelGroupMember> getCommandDispatcherFactory() {
					return factory.get();
				}
			});
		} catch (RuntimeException | Error e) {
			this.close();
			throw e;
		}
	}

	@Override
	public CacheContainerCommandDispatcherFactory get() {
		return this.factory;
	}
}
