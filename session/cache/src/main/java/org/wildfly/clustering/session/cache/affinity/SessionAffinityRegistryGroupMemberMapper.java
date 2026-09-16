/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.affinity;

import java.util.Map;

import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.server.GroupMember;
import org.wildfly.clustering.server.registry.Registry;

/**
 * Uses a registry to map a group member to a string value.
 * @param <M> the group member type
 * @author Paul Ferraro
 * @param <V> the registry value type
 */
public class SessionAffinityRegistryGroupMemberMapper<M extends GroupMember, V> implements Function<M, String> {

	private final Registry<M, String, V> registry;
	private final String localKey;

	/**
	 * Creates an affinity function from the specified registry.
	 * @param registry the registry storing session affinity entries
	 */
	public SessionAffinityRegistryGroupMemberMapper(Registry<M, String, V> registry) {
		this.registry = registry;
		this.localKey = registry.getEntry(registry.getGroup().getLocalMember()).getKey();
	}

	@Override
	public String apply(M member) {
		Map.Entry<String, V> entry = (member != null) ? this.registry.getEntry(member) : null;
		return (entry != null) ? entry.getKey() : this.localKey;
	}
}
