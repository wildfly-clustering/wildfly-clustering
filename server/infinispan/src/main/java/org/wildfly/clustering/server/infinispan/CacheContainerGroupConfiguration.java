/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheContainerConfiguration;

/**
 * Configuration requiring a cache container group.
 * @author Paul Ferraro
 */
public interface CacheContainerGroupConfiguration extends EmbeddedCacheContainerConfiguration {
	/**
	 * Returns the group of this configuration.
	 * @return the group of this configuration.
	 */
	CacheContainerGroup getGroup();
}
