/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local;

import java.util.List;

import org.wildfly.clustering.server.group.GroupMembership;

/**
 * A membership that only ever contains a single member.
 * @author Paul Ferraro
 */
public class SingletonMembership implements GroupMembership<LocalGroupMember> {

	private final List<LocalGroupMember> members;

	public SingletonMembership(LocalGroupMember member) {
		this.members = List.of(member);
	}

	@Override
	public int getCoordinatorIndex() {
		return 0;
	}

	@Override
	public List<LocalGroupMember> getMembers() {
		return this.members;
	}

	@Override
	public int hashCode() {
		return this.getCoordinator().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof SingletonMembership)) return false;
		SingletonMembership membership = (SingletonMembership) object;
		return this.members.equals(membership.members);
	}

	@Override
	public String toString() {
		return this.members.toString();
	}
}
