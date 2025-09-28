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

	/**
	 * Returns a locality that returns the same value for any key.
	 * @param local specifies whether or not all keys are local
	 * @return a locality that returns the same value for any key.
	 */
	static Locality of(boolean local) {
		return new SimpleLocality(local);
	}

	/**
	 * Returns the locality for the current consistent hash of the specified cache.
	 * @param cache an embedded cache
	 * @return the locality for the current consistent hash of the specified cache.
	 */
	static Locality forCurrentConsistentHash(Cache<?, ?> cache) {
		DistributionManager distribution = cache.getAdvancedCache().getDistributionManager();
		return (distribution != null) ? forConsistentHash(cache, distribution.getCacheTopology().getWriteConsistentHash()) : of(true);
	}

	/**
	 * Returns the locality for the specified consistent hash of the specified cache.
	 * @param cache an embedded cache
	 * @param hash a consistent hash
	 * @return the locality for the specified consistent hash of the specified cache.
	 */
	static Locality forConsistentHash(Cache<?, ?> cache, ConsistentHash hash) {
		return new ConsistentHashLocality(cache, hash);
	}
}
