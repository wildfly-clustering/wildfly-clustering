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
import org.wildfly.clustering.function.Supplier;

/**
 * Key distribution functions for a specific {@link ConsistentHash}.
 * @author Paul Ferraro
 */
public class ConsistentHashKeyDistribution implements KeyDistribution {

	private final DistributionManager distribution;
	private final Supplier<ConsistentHash> hash;

	ConsistentHashKeyDistribution(Cache<?, ?> cache) {
		this(cache, () -> cache.getAdvancedCache().getDistributionManager().getCacheTopology().getWriteConsistentHash());
	}

	ConsistentHashKeyDistribution(Cache<?, ?> cache, ConsistentHash hash) {
		this(cache, Supplier.of(hash));
	}

	private ConsistentHashKeyDistribution(Cache<?, ?> cache, Supplier<ConsistentHash> hash) {
		this(cache.getAdvancedCache().getDistributionManager(), hash);
	}

	ConsistentHashKeyDistribution(DistributionManager distribution, Supplier<ConsistentHash> hash) {
		this.distribution = distribution;
		this.hash = hash;
	}

	@Override
	public Address getPrimaryOwner(Object key) {
		int segment = this.distribution.getCacheTopology().getSegment(key);
		return this.hash.get().locatePrimaryOwnerForSegment(segment);
	}

	@Override
	public List<Address> getOwners(Object key) {
		int segment = this.distribution.getCacheTopology().getSegment(key);
		return this.hash.get().locateOwnersForSegment(segment);
	}
}
