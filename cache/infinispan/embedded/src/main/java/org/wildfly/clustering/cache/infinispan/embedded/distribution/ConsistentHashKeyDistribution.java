/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.distribution;

import java.util.List;
import java.util.function.Supplier;

import org.infinispan.Cache;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.distribution.ch.KeyPartitioner;
import org.infinispan.remoting.transport.Address;
import org.wildfly.common.function.Functions;

/**
 * Key distribution functions for a specific {@link ConsistentHash}.
 * @author Paul Ferraro
 */
public class ConsistentHashKeyDistribution implements KeyDistribution {

	private final KeyPartitioner partitioner;
	private final Supplier<ConsistentHash> hash;

	ConsistentHashKeyDistribution(Cache<?, ?> cache) {
		this(cache, () -> cache.getAdvancedCache().getDistributionManager().getCacheTopology().getWriteConsistentHash());
	}

	ConsistentHashKeyDistribution(Cache<?, ?> cache, ConsistentHash hash) {
		this(cache, Functions.constantSupplier(hash));
	}

	@SuppressWarnings("deprecation")
	private ConsistentHashKeyDistribution(Cache<?, ?> cache, Supplier<ConsistentHash> hash) {
		this(cache.getAdvancedCache().getComponentRegistry().getLocalComponent(KeyPartitioner.class), hash);
	}

	ConsistentHashKeyDistribution(KeyPartitioner partitioner, Supplier<ConsistentHash> hash) {
		this.partitioner = partitioner;
		this.hash = hash;
	}

	@Override
	public Address getPrimaryOwner(Object key) {
		int segment = this.partitioner.getSegment(key);
		return this.hash.get().locatePrimaryOwnerForSegment(segment);
	}

	@Override
	public List<Address> getOwners(Object key) {
		int segment = this.partitioner.getSegment(key);
		return this.hash.get().locateOwnersForSegment(segment);
	}
}
