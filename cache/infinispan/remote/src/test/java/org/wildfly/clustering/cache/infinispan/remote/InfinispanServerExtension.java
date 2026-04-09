/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import org.wildfly.clustering.cache.ContainerExtension;

/**
 * JUnit extension that manages the lifecycle of an Infinispan server container and configures a HotRod client.
 * @author Paul Ferraro
 */
public class InfinispanServerExtension extends ContainerExtension<InfinispanServerContainer> {

	public InfinispanServerExtension() {
		super(InfinispanServerContainer::new);
	}
}
