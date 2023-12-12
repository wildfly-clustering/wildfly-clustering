/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.distribution;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.distribution.ch.KeyPartitioner;
import org.infinispan.remoting.transport.Address;
import org.junit.jupiter.api.Test;
import org.wildfly.common.function.Functions;

/**
 * @author Paul Ferraro
 */
public class ConsistentHashKeyDistributionTestCase {

	@Test
	public void test() {
		KeyPartitioner partitioner = mock(KeyPartitioner.class);
		ConsistentHash hash = mock(ConsistentHash.class);
		KeyDistribution distribution = new ConsistentHashKeyDistribution(partitioner, Functions.constantSupplier(hash));

		Address address = mock(Address.class);
		Object key = new Object();
		int segment = 128;

		when(partitioner.getSegment(key)).thenReturn(segment);
		when(hash.locatePrimaryOwnerForSegment(segment)).thenReturn(address);

		Address result = distribution.getPrimaryOwner(key);

		assertSame(address, result);
	}
}
