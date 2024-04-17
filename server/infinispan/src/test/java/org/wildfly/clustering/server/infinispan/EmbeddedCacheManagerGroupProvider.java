/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.infinispan.manager.EmbeddedCacheManager;
import org.jgroups.JChannel;
import org.wildfly.clustering.server.group.Group;
import org.wildfly.clustering.server.jgroups.ChannelGroup;
import org.wildfly.clustering.server.jgroups.ChannelGroupMember;
import org.wildfly.clustering.server.jgroups.ForkChannelFactory;
import org.wildfly.clustering.server.jgroups.JChannelFactory;
import org.wildfly.clustering.server.jgroups.JChannelGroup;

/**
 * @author Paul Ferraro
 */
public class EmbeddedCacheManagerGroupProvider implements CacheContainerGroupProvider {
	private static final String CONTAINER_NAME = "container";

	private final JChannel channel;
	private final ChannelGroup channelGroup;
	private final EmbeddedCacheManager manager;
	private final CacheContainerGroup group;

	public EmbeddedCacheManagerGroupProvider(String clusterName, String memberName) throws Exception {
		this.channel = JChannelFactory.INSTANCE.apply(memberName);
		this.channel.connect(clusterName);
		this.channelGroup = new JChannelGroup(this.channel);
		this.manager = new EmbeddedCacheManagerFactory(new ForkChannelFactory(this.channel), clusterName, memberName).apply(CONTAINER_NAME, EmbeddedCacheManagerGroup.class.getClassLoader());
		this.group = new EmbeddedCacheManagerGroup<>(new ChannelEmbeddedCacheManagerGroupConfiguration() {
			@Override
			public Group<org.jgroups.Address, ChannelGroupMember> getGroup() {
				return EmbeddedCacheManagerGroupProvider.this.channelGroup;
			}

			@Override
			public EmbeddedCacheManager getCacheContainer() {
				return EmbeddedCacheManagerGroupProvider.this.manager;
			}
		});
	}

	@Override
	public EmbeddedCacheManager getCacheManager() {
		return this.manager;
	}

	@Override
	public JChannel getChannel() {
		return this.channel;
	}

	@Override
	public CacheContainerGroup getGroup() {
		return this.group;
	}

	@Override
	public String getName() {
		return CONTAINER_NAME;
	}

	@Override
	public void close() {
		try {
			this.manager.close();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		this.channelGroup.close();
		this.channel.disconnect();
		this.channel.close();
	}
}
