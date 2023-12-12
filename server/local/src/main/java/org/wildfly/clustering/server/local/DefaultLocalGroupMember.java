/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.local;

/**
 * Non-clustered {@link GroupMember} implementation.
 * @author Paul Ferraro
 */
class DefaultLocalGroupMember implements LocalGroupMember {

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
		if (!(object instanceof DefaultLocalGroupMember)) return false;
		DefaultLocalGroupMember member = (DefaultLocalGroupMember) object;
		return this.name.equals(member.name);
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
