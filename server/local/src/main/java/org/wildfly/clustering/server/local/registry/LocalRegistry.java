/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.registry;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.local.LocalGroup;
import org.wildfly.clustering.server.local.LocalGroupMember;
import org.wildfly.clustering.server.registry.Registry;
import org.wildfly.clustering.server.registry.RegistryListener;

/**
 * Local {@link Registry}.
 * @param <K> the registry key type
 * @param <V> the registry value type
 * @author Paul Ferraro
 */
public interface LocalRegistry<K, V> extends Registry<LocalGroupMember, K, V> {

	@Override
	LocalGroup getGroup();

	static <K, V> LocalRegistry<K, V> of(LocalGroup group, Map.Entry<K, V> entry, Runnable closeTask) {
		Map<K, V> entries = Collections.singletonMap(entry.getKey(), entry.getValue());
		AtomicBoolean closed = new AtomicBoolean(false);
		return new LocalRegistry<>() {
			@Override
			public LocalGroup getGroup() {
				return group;
			}

			@Override
			public Registration register(RegistryListener<K, V> object) {
				return Registration.EMPTY;
			}

			@Override
			public Map<K, V> getEntries() {
				return !closed.get() ? entries : Map.of();
			}

			@Override
			public Map.Entry<K, V> getEntry(LocalGroupMember member) {
				return !closed.get() && group.getLocalMember().equals(member) ? entry : null;
			}

			@Override
			public void close() {
				if (closed.compareAndSet(false, true)) {
					closeTask.run();
				}
			}
		};
	}
}
