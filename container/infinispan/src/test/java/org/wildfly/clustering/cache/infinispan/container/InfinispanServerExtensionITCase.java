/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache.infinispan.container;

import org.assertj.core.api.Assertions;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.wildfly.clustering.container.infinispan.InfinispanServerExtension;

/**
 * @author Paul Ferraro
 */
public class InfinispanServerExtensionITCase {

	@RegisterExtension
	static final InfinispanServerExtension INFINISPAN = new InfinispanServerExtension();

	@Test
	public void test() {
		Assertions.assertThat(INFINISPAN.getContainer().isCreated()).isTrue();
		Assertions.assertThat(INFINISPAN.getContainer().isHostAccessible()).isTrue();
		Assertions.assertThat(INFINISPAN.getContainer().isRunning()).isTrue();

		try (RemoteCacheManager manager = new RemoteCacheManager(INFINISPAN.getURI().toConfigurationBuilder().build())) {
			Assertions.assertThat(manager.isStarted()).isTrue();
		}
	}
}
