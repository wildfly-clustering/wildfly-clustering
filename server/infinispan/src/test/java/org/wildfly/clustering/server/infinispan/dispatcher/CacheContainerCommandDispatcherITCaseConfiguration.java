/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.dispatcher;

import org.infinispan.manager.EmbeddedCacheManager;
import org.jgroups.Address;
import org.wildfly.clustering.server.dispatcher.CommandDispatcherFactory;
import org.wildfly.clustering.server.group.GroupCommandDispatcherFactory;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.infinispan.EmbeddedCacheManagerFactory;
import org.wildfly.clustering.server.jgroups.ChannelGroupMember;
import org.wildfly.clustering.server.jgroups.ForkChannelFactory;
import org.wildfly.clustering.server.jgroups.dispatcher.ChannelCommandDispatcherITCaseConfiguration;
import org.wildfly.clustering.server.jgroups.dispatcher.CommandDispatcherITCaseConfiguration;

/**
 * @author Paul Ferraro
 */
public class CacheContainerCommandDispatcherITCaseConfiguration implements CommandDispatcherITCaseConfiguration<CacheContainerGroupMember> {
	private static final String CONTAINER_NAME = "container";

	private final ChannelCommandDispatcherITCaseConfiguration config;
	private final EmbeddedCacheManager manager;
	private final CommandDispatcherFactory<CacheContainerGroupMember> factory;

	public CacheContainerCommandDispatcherITCaseConfiguration(String clusterName, String memberName) throws Exception {
		this.config = new ChannelCommandDispatcherITCaseConfiguration(clusterName, memberName);
		this.manager = new EmbeddedCacheManagerFactory(new ForkChannelFactory(this.config.getChannel()), clusterName, memberName).apply(CONTAINER_NAME);
		this.factory = new EmbeddedCacheManagerCommandDispatcherFactory<>(new ChannelEmbeddedCacheManagerCommandDispatcherFactoryConfiguration() {
			@Override
			public EmbeddedCacheManager getCacheContainer() {
				return CacheContainerCommandDispatcherITCaseConfiguration.this.manager;
			}
			
			@Override
			public GroupCommandDispatcherFactory<Address, ChannelGroupMember> getCommandDispatcherFactory() {
				return CacheContainerCommandDispatcherITCaseConfiguration.this.config.getCommandDispatcherFactory();
			}
		});
	}

	@Override
	public CommandDispatcherFactory<CacheContainerGroupMember> getCommandDispatcherFactory() {
		return this.factory;
	}

	@Override
	public void close() throws Exception {
		this.manager.close();
		this.config.close();
	}
}
