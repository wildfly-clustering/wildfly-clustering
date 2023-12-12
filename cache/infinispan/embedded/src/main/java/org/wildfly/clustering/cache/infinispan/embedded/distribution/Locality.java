/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache.infinispan.embedded.distribution;

import org.infinispan.Cache;
import org.infinispan.distribution.DistributionManager;
import org.infinispan.distribution.ch.ConsistentHash;

/**
 * Facility for determining the primary ownership/location of a given cache key.
 * @author Paul Ferraro
 */
public interface Locality {
	/**
	 * Indicates whether the current node is the primary owner of the specified cache key.
	 * For local caches, this method will always return true.
	 * @param key a cache key
	 * @return true, if the current node is the primary owner of the specified cache key, false otherwise
	 */
	boolean isLocal(Object key);

	static Locality of(boolean local) {
		return new SimpleLocality(local);
	}

	static Locality forCurrentConsistentHash(Cache<?, ?> cache) {
		DistributionManager distribution = cache.getAdvancedCache().getDistributionManager();
		return (distribution != null) ? forConsistentHash(cache, distribution.getCacheTopology().getWriteConsistentHash()) : of(true);
	}

	static Locality forConsistentHash(Cache<?, ?> cache, ConsistentHash hash) {
		return new ConsistentHashLocality(cache, hash);
	}
}
