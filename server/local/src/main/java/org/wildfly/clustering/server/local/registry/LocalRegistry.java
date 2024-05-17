/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.registry;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.wildfly.clustering.server.Group;
import org.wildfly.clustering.server.GroupMember;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.registry.Registry;
import org.wildfly.clustering.server.registry.RegistryListener;

/**
 * Local {@link Registry}.
 * @param <M> the group member type
 * @param <K> the registry key type
 * @param <V> the registry value type
 * @author Paul Ferraro
 */
public class LocalRegistry<M extends GroupMember, K, V> implements Registry<M, K, V> {

	private final Group<M> group;
	private final Runnable closeTask;
	private final AtomicBoolean closed = new AtomicBoolean(false);
	private final Map.Entry<K, V> entry;
	private final Map<K, V> entries;

	public LocalRegistry(Group<M> group, Map.Entry<K, V> entry, Runnable closeTask) {
		this.group = group;
		this.entry = entry;
		this.entries = Collections.singletonMap(entry.getKey(), entry.getValue());
		this.closeTask = closeTask;
	}

	@Override
	public Registration register(RegistryListener<K, V> object) {
		return Registration.EMPTY;
	}

	@Override
	public Group<M> getGroup() {
		return this.group;
	}

	@Override
	public Map<K, V> getEntries() {
		return !this.closed.get() ? this.entries : Map.of();
	}

	@Override
	public Map.Entry<K, V> getEntry(M member) {
		return !this.closed.get() && this.group.getLocalMember().equals(member) ? this.entry : null;
	}

	@Override
	public void close() {
		if (this.closed.compareAndSet(false, true)) {
			this.closeTask.run();
		}
	}
}
