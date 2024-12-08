/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.distribution;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.infinispan.remoting.transport.Address;
import org.junit.jupiter.api.Test;

/**
 * @author Paul Ferraro
 */
public class ConsistentHashLocalityTestCase {

	@Test
	public void test() {
		KeyDistribution distribution = mock(KeyDistribution.class);
		Address localAddress = mock(Address.class);
		Address remoteAddress = mock(Address.class);
		Object localKey = new Object();
		Object remoteKey = new Object();

		Locality locality = new ConsistentHashLocality(distribution, localAddress);

		when(distribution.getPrimaryOwner(localKey)).thenReturn(localAddress);
		when(distribution.getPrimaryOwner(remoteKey)).thenReturn(remoteAddress);

		assertThat(locality.isLocal(localKey)).isTrue();
		assertThat(locality.isLocal(remoteKey)).isFalse();
	}
}
