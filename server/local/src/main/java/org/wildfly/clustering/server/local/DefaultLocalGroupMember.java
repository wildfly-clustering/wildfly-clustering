/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.local;

import org.wildfly.clustering.server.group.AbstractGroupMember;

/**
 * Non-clustered {@link GroupMember} implementation.
 * @author Paul Ferraro
 */
class DefaultLocalGroupMember extends AbstractGroupMember<String> implements LocalGroupMember {

	private final String name;

	/**
	 * Creates a new local group member with the specified name
	 * @param name the group member name
	 */
	DefaultLocalGroupMember(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}
}
