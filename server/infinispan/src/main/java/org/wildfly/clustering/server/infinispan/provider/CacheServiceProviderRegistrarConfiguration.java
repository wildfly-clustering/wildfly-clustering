/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.infinispan.provider;

import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.server.infinispan.CacheContainerGroup;

/**
 * Configuration for a {@link CacheServiceProviderRegistry}.
 * @author Paul Ferraro
 */
public interface CacheServiceProviderRegistryConfiguration extends EmbeddedCacheConfiguration {
	Object getId();
	CacheContainerGroup getGroup();
}
