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

	DefaultLocalGroupMember(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof LocalGroupMember member)) return false;
		return this.name.equals(member.getName());
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public String toString() {
		return this.name;
	}
}
