/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.infinispan.registry;

import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.server.infinispan.CacheContainerGroup;

/**
 * Configuration for a {@link CacheRegistryFactory}.
 * @author Paul Ferraro
 */
public interface CacheRegistryConfiguration extends EmbeddedCacheConfiguration {
	CacheContainerGroup getGroup();
}
