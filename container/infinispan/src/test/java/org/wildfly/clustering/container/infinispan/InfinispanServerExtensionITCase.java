/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.container.infinispan;

import org.assertj.core.api.Assertions;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.wildfly.clustering.container.DefaultContainer;

/**
 * @author Paul Ferraro
 */
public class InfinispanServerExtensionITCase {
	private static final int CLUSTER_SIZE = 2;
	@RegisterExtension
	static final InfinispanServerExtension INFINISPAN = new InfinispanServerExtension(CLUSTER_SIZE);

	@Test
	public void test() {
		for (DefaultContainer container : INFINISPAN.getContainers()) {
			Assertions.assertThat(container.isCreated()).isTrue();
			Assertions.assertThat(container.isHostAccessible()).isTrue();
			Assertions.assertThat(container.isRunning()).isTrue();
		}

		try (RemoteCacheManager manager = new RemoteCacheManager(INFINISPAN.getURI().toConfigurationBuilder().build())) {
			Assertions.assertThat(manager.isStarted()).isTrue();
			Assertions.assertThat(manager.getServers()).hasSize(CLUSTER_SIZE);
		}
	}
}
