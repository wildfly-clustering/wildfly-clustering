/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.affinity;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

import org.infinispan.AdvancedCache;
import org.infinispan.affinity.KeyAffinityService;
import org.infinispan.affinity.KeyGenerator;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.remoting.transport.Address;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.KeyDistribution;

/**
 * Unit test for {@link DefaultKeyAffinityService}.
 * @author Paul Ferraro
 */
public class DefaultKeyAffinityServiceTestCase {

	private static final int SEGMENTS = 3;
	private static final int LOCAL_SEGMENT = 0;
	private static final int REMOTE_SEGMENT = 1;
	private static final int FILTERED_SEGMENT = 2;

	@Test
	public void test() {
		KeyGenerator<UUID> generator = mock(KeyGenerator.class);
		AdvancedCache<UUID, Object> cache = mock(AdvancedCache.class);
		KeyDistribution distribution = mock(KeyDistribution.class);
		ConsistentHash hash = mock(ConsistentHash.class);
		Address local = mock(Address.class);
		Address remote = mock(Address.class);
		Address standby = mock(Address.class);
		Address ignored = mock(Address.class);
		UUID random = UUID.randomUUID();
		KeyAffinityService<UUID> service = new DefaultKeyAffinityService<>(cache, generator, address -> (address != ignored), c -> hash, (c, h) -> distribution);

		doReturn(random).when(generator).getKey();

		// Validate that service returns random key when not started
		assertThat(service.getKeyForAddress(local)).isSameAs(random);
		assertThat(service.getKeyForAddress(remote)).isSameAs(random);
		assertThat(service.getKeyForAddress(standby)).isSameAs(random);
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> service.getKeyForAddress(ignored));

		List<Address> members = List.of(local, remote, ignored, standby);

		when(hash.getMembers()).thenReturn(members);
		when(hash.getPrimarySegmentsForOwner(local)).thenReturn(Set.of(LOCAL_SEGMENT));
		when(hash.getPrimarySegmentsForOwner(remote)).thenReturn(Set.of(REMOTE_SEGMENT));
		when(hash.getPrimarySegmentsForOwner(standby)).thenReturn(Set.of());
		when(hash.getPrimarySegmentsForOwner(ignored)).thenReturn(Set.of(FILTERED_SEGMENT));

		// Mock a sufficient number of keys
		int[] keysPerSegment = new int[3];
		Arrays.fill(keysPerSegment, 0);
		int minKeysPerSegment = DefaultKeyAffinityService.DEFAULT_QUEUE_SIZE * SEGMENTS;
		IntPredicate needMoreKeys = keys -> (keys < minKeysPerSegment);
		OngoingStubbing<UUID> stub = when(generator.getKey());
		while (IntStream.of(keysPerSegment).anyMatch(needMoreKeys)) {
			UUID key = UUID.randomUUID();
			int segment = getSegment(key);
			keysPerSegment[segment] += 1;

			stub = stub.thenReturn(key);

			when(distribution.getPrimaryOwner(key)).thenReturn(members.get(segment));
		}

		// This should throw IAE, since address does not pass filter
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> service.getKeyForAddress(ignored));

		service.start();

		try {
			int iterations = DefaultKeyAffinityService.DEFAULT_QUEUE_SIZE / 2;
			for (int i = 0; i < iterations; ++i) {
				UUID key = service.getKeyForAddress(local);
				int segment = getSegment(key);
				assertThat(segment).isEqualTo(LOCAL_SEGMENT);

				key = service.getCollocatedKey(key);
				segment = getSegment(key);
				assertThat(segment).isEqualTo(LOCAL_SEGMENT);

				key = service.getKeyForAddress(remote);
				segment = getSegment(key);
				assertThat(segment).isEqualTo(REMOTE_SEGMENT);

				key = service.getCollocatedKey(key);
				segment = getSegment(key);
				assertThat(segment).isEqualTo(REMOTE_SEGMENT);
			}

			// This should return a random key
			assertThat(service.getKeyForAddress(standby)).isNotNull();
			// This should throw IAE, since address does not pass filter
			assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> service.getKeyForAddress(ignored));
		} finally {
			service.stop();
		}
	}

	private static int getSegment(UUID key) {
		return Math.abs(key.hashCode()) % SEGMENTS;
	}
}
