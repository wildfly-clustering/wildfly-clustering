/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.manager.EmbeddedCacheManager;
import org.jgroups.JChannel;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheManagerContext;
import org.wildfly.clustering.context.AbstractContext;
import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.server.group.Group;
import org.wildfly.clustering.server.jgroups.ChannelGroup;
import org.wildfly.clustering.server.jgroups.ChannelGroupContext;
import org.wildfly.clustering.server.jgroups.ChannelGroupMember;

/**
 * @author Paul Ferraro
 */
public class EmbeddedCacheManagerGroupContext extends AbstractContext<CacheContainerGroup> {

	private final CacheContainerGroup group;

	public EmbeddedCacheManagerGroupContext(String clusterName, String memberName) {
		this(new ChannelGroupContext(clusterName, memberName));
	}

	public EmbeddedCacheManagerGroupContext(JChannel channel) {
		this(new ChannelGroupContext(channel));
	}

	private EmbeddedCacheManagerGroupContext(Context<ChannelGroup> group) {
		this.accept(group::close);
		try {
			Context<EmbeddedCacheManager> manager = new EmbeddedCacheManagerContext(group.get().getChannel());
			this.accept(manager::close);
			this.group = new EmbeddedCacheManagerGroup<>(new ChannelEmbeddedCacheManagerGroupConfiguration() {
				@Override
				public Group<org.jgroups.Address, ChannelGroupMember> getGroup() {
					return group.get();
				}

				@Override
				public EmbeddedCacheManager getCacheContainer() {
					return manager.get();
				}
			});
		} catch (RuntimeException | Error e) {
			this.close();
			throw e;
		}
	}

	@Override
	public CacheContainerGroup get() {
		return this.group;
	}
}
