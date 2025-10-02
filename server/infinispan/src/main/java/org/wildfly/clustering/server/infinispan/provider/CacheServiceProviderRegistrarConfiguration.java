/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.infinispan.provider;

import org.wildfly.clustering.server.infinispan.CacheGroupConfiguration;

/**
 * Configuration for a {@link CacheServiceProviderRegistrar}.
 * @author Paul Ferraro
 */
public interface CacheServiceProviderRegistrarConfiguration extends CacheGroupConfiguration {
	/**
	 * Returns the unique identifier of this service provider registrar.
	 * @return the unique identifier of this service provider registrar.
	 */
	Object getId();
}
