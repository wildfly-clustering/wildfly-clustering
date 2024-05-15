/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.registry;

import java.util.Map;

import org.wildfly.clustering.server.Group;
import org.wildfly.clustering.server.GroupMember;
import org.wildfly.clustering.server.Registrar;

/**
 * Registry of entries specific to a group member.
 * @param <M> the member type
 * @param <K> the type of the registry entry key
 * @param <V> the type of the registry entry value
 * @author Paul Ferraro
 */
public interface Registry<M extends GroupMember, K, V> extends Registrar<RegistryListener<K, V>>, AutoCloseable {

	/**
	 * Returns the group associated with this factory.
	 *
	 * @return a group
	 */
	Group<M> getGroup();

	/**
	 * Returns all registry entries in this group.
	 *
	 * @return a map for entries
	 */
	Map<K, V> getEntries();

	/**
	 * Returns the registry entry for the specified group member.
	 *
	 * @param member a group member
	 * @return the registry entry of the specified group member, or null if undefined.
	 */
	Map.Entry<K, V> getEntry(M member);

	@Override
	void close();
}
