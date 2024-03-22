/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.affinity;

import org.infinispan.Cache;
import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.server.infinispan.CacheContainerGroup;

/**
 * Encapsulates group member affinity configuration.
 * @param <I> the identifier type of a cache key
 * @author Paul Ferraro
 */
public interface GroupMemberAffinityConfiguration<I> {

	Cache<? extends Key<I>, ?> getCache();

	CacheContainerGroup getGroup();
}
