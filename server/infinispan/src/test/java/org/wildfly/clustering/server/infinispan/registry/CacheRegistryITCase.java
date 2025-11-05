/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.registry;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.registry.Registry;
import org.wildfly.clustering.server.registry.RegistryListener;

/**
 * @author Paul Ferraro
 */
public class CacheRegistryITCase {
	private static final Duration EVENT_DURATION = Duration.ofSeconds(1);
	private static final String CLUSTER_NAME = "cluster";
	private static final String MEMBER_1 = "member1";
	private static final String MEMBER_2 = "member2";

	@Test
	public void test() throws Exception {
		Map.Entry<String, UUID> entry1 = Map.entry("foo", UUID.randomUUID());
		Map.Entry<String, UUID> entry2 = Map.entry("bar", UUID.randomUUID());
		try (CacheContainerRegistryFactoryContext<String, UUID> factory1 = new CacheContainerRegistryFactoryContext<>(CLUSTER_NAME, MEMBER_1)) {
			try (Registry<CacheContainerGroupMember, String, UUID> registry1 = factory1.get().createRegistry(entry1)) {
				CacheContainerGroupMember member1 = registry1.getGroup().getLocalMember();
				assertThat(registry1.getEntry(member1)).isEqualTo(entry1);
				assertThat(registry1.getEntries()).containsExactlyEntriesOf(Map.ofEntries(entry1));

				BlockingQueue<Map<String, UUID>> additions = new LinkedBlockingQueue<>();
				BlockingQueue<Map<String, UUID>> updates = new LinkedBlockingQueue<>();
				BlockingQueue<Map<String, UUID>> removals = new LinkedBlockingQueue<>();
				RegistryListener<String, UUID> listener = new RegistryListener<>() {
					@Override
					public void added(Map<String, UUID> added) {
						additions.add(added);
					}

					@Override
					public void updated(Map<String, UUID> updated) {
						updates.add(updated);
					}

					@Override
					public void removed(Map<String, UUID> removed) {
						removals.add(removed);
					}
				};
				try (Registration registration = registry1.register(listener)) {

					assertThat(additions).isEmpty();
					assertThat(updates).isEmpty();
					assertThat(removals).isEmpty();

					try (CacheContainerRegistryFactoryContext<String, UUID> factory2 = new CacheContainerRegistryFactoryContext<>(CLUSTER_NAME, MEMBER_2)) {
						try (Registry<CacheContainerGroupMember, String, UUID> registry2 = factory2.get().createRegistry(entry2)) {

							CacheContainerGroupMember member2 = registry2.getGroup().getLocalMember();

							assertThat(registry1.getEntry(member1)).isEqualTo(entry1);
							assertThat(registry2.getEntry(member1)).isEqualTo(entry1);
							assertThat(registry1.getEntry(member2)).isEqualTo(entry2);
							assertThat(registry2.getEntry(member2)).isEqualTo(entry2);

							assertThat(registry1.getEntries()).containsExactlyInAnyOrderEntriesOf(Map.ofEntries(entry1, entry2));
							assertThat(registry2.getEntries()).containsExactlyInAnyOrderEntriesOf(Map.ofEntries(entry1, entry2));

							assertThat(additions.poll(EVENT_DURATION.toMillis(), TimeUnit.MILLISECONDS)).isEqualTo(Map.ofEntries(entry2));
							assertThat(additions).isEmpty();
							assertThat(updates).isEmpty();
							assertThat(removals).isEmpty();
						}
					}

					assertThat(registry1.getEntry(member1)).isEqualTo(entry1);
					assertThat(registry1.getEntries()).containsExactlyEntriesOf(Map.ofEntries(entry1));

					assertThat(additions).isEmpty();
					assertThat(updates).isEmpty();
					assertThat(removals.poll(EVENT_DURATION.toMillis(), TimeUnit.MILLISECONDS)).isEqualTo(Map.ofEntries(entry2));
					assertThat(removals).isEmpty();
				}
			}
		}
	}
}
