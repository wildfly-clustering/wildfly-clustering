/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local;

import org.wildfly.clustering.server.group.GroupMembership;

/**
 * Non-clustered group implementation.
 * Registered {@link GroupListener} are never invoked, as membership of a local group is fixed.
 * @author Paul Ferraro
 */
class DefaultLocalGroup implements LocalGroup {

	private final String name;
	private final LocalGroupMember member;
	private final GroupMembership<LocalGroupMember> membership;
	private final LocalGroupMemberFactory factory;

	DefaultLocalGroup(String groupName, String memberName) {
		this.name = groupName;
		this.member = new DefaultLocalGroupMember(memberName);
		this.membership = new SingletonMembership(this.member);
		this.factory = id -> this.member;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public LocalGroupMember getLocalMember() {
		return this.member;
	}

	@Override
	public GroupMembership<LocalGroupMember> getMembership() {
		return this.membership;
	}

	@Override
	public LocalGroupMemberFactory getGroupMemberFactory() {
		return this.factory;
	}
}
