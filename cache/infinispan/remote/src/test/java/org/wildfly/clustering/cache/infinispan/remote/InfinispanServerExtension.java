/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.wildfly.clustering.cache.ContainerExtension;

/**
 * JUnit extension that manages the lifecycle of an Infinispan server container and configures a HotRod client.
 * @author Paul Ferraro
 */
public class InfinispanServerExtension extends ContainerExtension<InfinispanServerContainer> implements RemoteCacheContainerConfigurator {

	public InfinispanServerExtension() {
		super(InfinispanServerContainer::new);
	}

	@Override
	public Configuration configure(ConfigurationBuilder builder) {
		InfinispanServerContainer container = this.getContainer();
		return builder.security()
			.authentication()
				.username(container.getUsername())
				.password(container.getPassword())
			.addServer()
				.host(container.getHost())
				.port(container.getPort())
			.build();
	}
}
