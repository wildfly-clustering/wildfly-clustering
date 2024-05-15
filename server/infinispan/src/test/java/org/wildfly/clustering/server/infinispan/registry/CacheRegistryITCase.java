/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.registry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.registry.Registry;
import org.wildfly.clustering.server.registry.RegistryListener;

/**
 * @author Paul Ferraro
 */
public class CacheRegistryITCase {
	private static final String CLUSTER_NAME = "cluster";
	private static final String MEMBER_1 = "member1";
	private static final String MEMBER_2 = "member2";

	@Test
	public void test() throws Exception {
		Map.Entry<String, UUID> entry1 = Map.entry("foo", UUID.randomUUID());
		Map.Entry<String, UUID> entry2 = Map.entry("bar", UUID.randomUUID());
		try (CacheContainerRegistryProvider<String, UUID> provider1 = new CacheContainerRegistryProvider<>(CLUSTER_NAME, MEMBER_1)) {
			try (Registry<CacheContainerGroupMember, String, UUID> registry1 = provider1.apply(entry1)) {
				CacheContainerGroupMember member1 = registry1.getGroup().getLocalMember();
				assertEquals(entry1, registry1.getEntry(member1));
				assertEquals(Map.ofEntries(entry1), registry1.getEntries());

				RegistryListener<String, UUID> listener = mock(RegistryListener.class);
				try (Registration registration = registry1.register(listener)) {

					verifyNoInteractions(listener);

					try (CacheContainerRegistryProvider<String, UUID> provider2 = new CacheContainerRegistryProvider<>(CLUSTER_NAME, MEMBER_2)) {
						try (Registry<CacheContainerGroupMember, String, UUID> registry2 = provider2.apply(entry2)) {
							CacheContainerGroupMember member2 = registry2.getGroup().getLocalMember();

							assertEquals(entry1, registry1.getEntry(member1));
							assertEquals(entry1, registry2.getEntry(member1));
							assertEquals(entry2, registry1.getEntry(member2));
							assertEquals(entry2, registry2.getEntry(member2));

							assertEquals(Map.ofEntries(entry1, entry2), registry1.getEntries());
							assertEquals(Map.ofEntries(entry1, entry2), registry2.getEntries());

							Thread.sleep(100);

							verify(listener).added(Map.ofEntries(entry2));
							verifyNoMoreInteractions(listener);
						}
					}

					assertEquals(entry1, registry1.getEntry(member1));
					assertEquals(Map.ofEntries(entry1), registry1.getEntries());

					Thread.sleep(100);

					verify(listener).removed(Map.ofEntries(entry2));
					verifyNoMoreInteractions(listener);
				}
			}
		}
	}
}
