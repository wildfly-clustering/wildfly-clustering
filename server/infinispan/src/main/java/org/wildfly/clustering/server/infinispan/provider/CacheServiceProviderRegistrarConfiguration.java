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
	/**
	 * Returns the unique identifier of this service provider registrar.
	 * @return the unique identifier of this service provider registrar.
	 */
	Object getId();

	/**
	 * Returns the cache container group associated with this service provider registrar.
	 * @return the cache container group associated with this service provider registrar.
	 */
	CacheContainerGroup getGroup();
}
