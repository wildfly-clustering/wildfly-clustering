/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;

/**
 * Configuration requiring a cache and group.
 * @author Paul Ferraro
 */
public interface CacheGroupConfiguration extends CacheContainerGroupConfiguration, EmbeddedCacheConfiguration {
}
