/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.registry;

import java.util.Map;

import org.wildfly.clustering.server.GroupMember;

/**
 * Factory for creating a clustered registry.
 *
 * @param <K> the type of the registry entry key
 * @param <V> the type of the registry entry value
 * @author Paul Ferraro
 */
public interface RegistryFactory<K, V, M extends GroupMember> {

	/**
	 * Creates a registry using the specified entry.
	 *
	 * @param entry the local registry entry
	 * @return a registry
	 */
	Registry<K, V, M> createRegistry(Map.Entry<K, V> entry);
}
