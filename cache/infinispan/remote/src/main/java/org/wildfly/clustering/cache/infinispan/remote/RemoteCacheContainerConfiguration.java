/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.wildfly.clustering.cache.infinispan.BasicCacheContainerConfiguration;

/**
 * A configuration with an associated remote cache container.
 * @author Paul Ferraro
 */
public interface RemoteCacheContainerConfiguration extends BasicCacheContainerConfiguration {

	@Override
	RemoteCacheContainer getCacheContainer();

	@Override
	default String getName() {
		return this.getCacheContainer().getConfiguration().statistics().jmxName();
	}
}
