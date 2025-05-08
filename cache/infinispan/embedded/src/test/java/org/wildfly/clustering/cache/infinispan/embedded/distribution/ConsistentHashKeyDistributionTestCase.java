/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.distribution;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.infinispan.distribution.DistributionManager;
import org.infinispan.distribution.LocalizedCacheTopology;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.distribution.ch.KeyPartitioner;
import org.infinispan.remoting.transport.Address;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.function.Supplier;

/**
 * @author Paul Ferraro
 */
public class ConsistentHashKeyDistributionTestCase {

	@Test
	public void test() {
		KeyPartitioner partitioner = mock(KeyPartitioner.class);
		DistributionManager dist = mock(DistributionManager.class);
		ConsistentHash hash = mock(ConsistentHash.class);
		KeyDistribution distribution = new ConsistentHashKeyDistribution(dist, Supplier.of(hash));

		Address address = mock(Address.class);
		Object key = new Object();
		int segment = 4;
		LocalizedCacheTopology topology = LocalizedCacheTopology.makeSegmentedSingletonTopology(partitioner, 8, address);

		doReturn(topology).when(dist).getCacheTopology();
		doReturn(segment).when(partitioner).getSegment(key);
		doReturn(address).when(hash).locatePrimaryOwnerForSegment(segment);
		when(hash.locatePrimaryOwnerForSegment(segment)).thenReturn(address);

		Address result = distribution.getPrimaryOwner(key);

		assertThat(result).isSameAs(address);
	}
}
