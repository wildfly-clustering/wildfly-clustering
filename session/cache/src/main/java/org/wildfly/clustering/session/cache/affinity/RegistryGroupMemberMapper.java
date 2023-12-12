/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.affinity;

import java.util.Map;
import java.util.function.Function;

import org.wildfly.clustering.server.GroupMember;
import org.wildfly.clustering.server.registry.Registry;

/**
 * Uses a registry to map a group member to a string value.
 * @author Paul Ferraro
 */
public class RegistryGroupMemberMapper<M extends GroupMember> implements Function<M, String> {

	private final Registry<String, Void, M> registry;
	private final String localKey;

	RegistryGroupMemberMapper(Registry<String, Void, M> registry) {
		this.registry = registry;
		this.localKey = registry.getEntry(registry.getGroup().getLocalMember()).getKey();
	}

	@Override
	public String apply(M member) {
		Map.Entry<String, Void> entry = this.registry.getEntry(member);
		return (entry != null) ? entry.getKey() : this.localKey;
	}
}
