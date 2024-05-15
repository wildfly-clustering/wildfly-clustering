/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.infinispan.provider;

import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.server.infinispan.CacheContainerGroup;

/**
 * Configuration for a {@link CacheServiceProviderRegistrar}.
 * @author Paul Ferraro
 */
public interface CacheServiceProviderRegistrarConfiguration extends EmbeddedCacheConfiguration {
	Object getId();
	CacheContainerGroup getGroup();
}
