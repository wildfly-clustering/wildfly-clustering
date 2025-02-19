/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.affinity;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.infinispan.affinity.KeyAffinityService;
import org.infinispan.affinity.KeyGenerator;
import org.infinispan.remoting.transport.Address;
import org.junit.jupiter.api.Test;

/**
 * @author Paul Ferraro
 */
public class SimpleKeyAffinityServiceTestCase {

	private final KeyGenerator<UUID> generator = mock(KeyGenerator.class);
	private final KeyAffinityService<UUID> service = new SimpleKeyAffinityService<>(this.generator);

	@Test
	public void getKeyForAddress() {
		Address address = mock(Address.class);
		UUID key = UUID.randomUUID();

		when(this.generator.getKey()).thenReturn(key);

		UUID result = this.service.getKeyForAddress(address);

		assertThat(result).isSameAs(key);
	}

	@Test
	public void getCollocatedKey() {
		UUID key = UUID.randomUUID();

		when(this.generator.getKey()).thenReturn(key);

		UUID result = this.service.getCollocatedKey(UUID.randomUUID());

		assertThat(result).isSameAs(key);
	}
}
