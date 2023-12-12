/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.distribution;

import java.util.List;

import org.infinispan.Cache;
import org.infinispan.distribution.DistributionManager;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.remoting.transport.Address;

/**
 * Provides key distribution functions.
 * @author Paul Ferraro
 */
public interface KeyDistribution {

	/**
	 * Returns the primary owner of the specified key.
	 * @param key a cache key
	 * @return the address of the primary owner
	 */
	Address getPrimaryOwner(Object key);

	/**
	 * Returns the owners of the specified key.
	 * @param key a cache key
	 * @return a list of addresses for each owner
	 */
	List<Address> getOwners(Object key);

	static KeyDistribution forCache(Cache<?, ?> cache) {
		DistributionManager distribution = cache.getAdvancedCache().getDistributionManager();
		return (distribution != null) ? new ConsistentHashKeyDistribution(cache) : LocalKeyDistribution.INSTANCE;
	}

	static KeyDistribution forConsistentHash(Cache<?, ?> cache, ConsistentHash hash) {
		return new ConsistentHashKeyDistribution(cache, hash);
	}
}
