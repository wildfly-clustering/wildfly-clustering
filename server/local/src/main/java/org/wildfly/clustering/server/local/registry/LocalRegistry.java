/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.registry;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.local.LocalGroup;
import org.wildfly.clustering.server.local.LocalGroupMember;
import org.wildfly.clustering.server.registry.Registry;
import org.wildfly.clustering.server.registry.RegistryListener;

/**
 * @author Paul Ferraro
 */
public class LocalRegistry<K, V> implements Registry<K, V, LocalGroupMember>, Function<Map.Entry<K, V>, Map<K, V>> {

	private final LocalGroup group;
	private final Runnable closeTask;

	private volatile Map.Entry<K, V> entry;

	public LocalRegistry(LocalGroup group, Map.Entry<K, V> entry, Runnable closeTask) {
		this.group = group;
		this.entry = entry;
		this.closeTask = closeTask;
	}

	@Override
	public Registration register(RegistryListener<K, V> object) {
		return Registration.EMPTY;
	}

	@Override
	public LocalGroup getGroup() {
		return this.group;
	}

	@Override
	public Map<K, V> getEntries() {
		return Optional.ofNullable(this.entry).map(this).orElse(Map.of());
	}

	@Override
	public Map.Entry<K, V> getEntry(LocalGroupMember member) {
		return this.group.getLocalMember().equals(member) ? this.entry : null;
	}

	@Override
	public void close() {
		this.entry = null;
		this.closeTask.run();
	}

	@Override
	public Map<K, V> apply(Map.Entry<K, V> entry) {
		return Map.of(entry.getKey(), entry.getValue());
	}
}
