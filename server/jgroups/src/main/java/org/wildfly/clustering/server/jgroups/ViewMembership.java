/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import java.util.List;
import java.util.stream.Collectors;

import org.jgroups.Address;
import org.jgroups.View;
import org.wildfly.clustering.server.group.GroupMembership;

/**
 * A group membership based on a JGroups view.
 * @author Paul Ferraro
 */
public class ViewMembership implements GroupMembership<ChannelGroupMember> {

	private final long id;
	private final List<ChannelGroupMember> members;
	private final int coordinatorIndex;

	public ViewMembership(View view, ChannelGroupMemberFactory factory) {
		this.id = view.getViewId().getId();
		List<Address> addresses = view.getMembers();
		this.members = addresses.stream().map(factory::createGroupMember).collect(Collectors.toUnmodifiableList());
		this.coordinatorIndex = addresses.indexOf(view.getCoord());
	}

	@Override
	public int getCoordinatorIndex() {
		return this.coordinatorIndex;
	}

	@Override
	public List<ChannelGroupMember> getMembers() {
		return this.members;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(this.id);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof ViewMembership)) return false;
		ViewMembership membership = (ViewMembership) object;
		return this.id == membership.id;
	}

	@Override
	public String toString() {
		return this.members.toString();
	}
}
