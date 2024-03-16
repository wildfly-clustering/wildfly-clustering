/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;
import org.jgroups.JChannel;
import org.wildfly.clustering.server.AutoCloseableProvider;
import org.wildfly.clustering.server.group.Group;
import org.wildfly.clustering.server.jgroups.ChannelGroup;
import org.wildfly.clustering.server.jgroups.ChannelGroupMember;
import org.wildfly.clustering.server.jgroups.ForkChannelFactory;
import org.wildfly.clustering.server.jgroups.GroupProvider;
import org.wildfly.clustering.server.jgroups.JChannelFactory;
import org.wildfly.clustering.server.jgroups.JChannelGroup;

/**
 * @author Paul Ferraro
 */
public class CacheContainerGroupProvider extends AutoCloseableProvider implements GroupProvider<Address, CacheContainerGroupMember> {
	private static final String CONTAINER_NAME = "container";

	private final JChannel channel;
	private final CacheContainerGroup group;

	public CacheContainerGroupProvider(String clusterName, String memberName) throws Exception {
		this.channel = JChannelFactory.INSTANCE.apply(memberName);
		this.accept(this.channel::close);
		this.channel.connect(clusterName);
		this.accept(this.channel::disconnect);
		ChannelGroup channelGroup = new JChannelGroup(this.channel);
		this.accept(channelGroup::close);
		EmbeddedCacheManager manager = new EmbeddedCacheManagerFactory(new ForkChannelFactory(this.channel), clusterName, memberName).apply(CONTAINER_NAME, EmbeddedCacheManagerGroup.class.getClassLoader());
		manager.start();
		this.accept(manager::stop);
		this.group = new EmbeddedCacheManagerGroup<>(new ChannelEmbeddedCacheManagerGroupConfiguration() {
			@Override
			public Group<org.jgroups.Address, ChannelGroupMember> getGroup() {
				return channelGroup;
			}

			@Override
			public EmbeddedCacheManager getCacheContainer() {
				return manager;
			}
		});
	}

	@Override
	public JChannel getChannel() {
		return this.channel;
	}

	@Override
	public Group<Address, CacheContainerGroupMember> getGroup() {
		return this.group;
	}

	@Override
	public String getName() {
		return CONTAINER_NAME;
	}
}
