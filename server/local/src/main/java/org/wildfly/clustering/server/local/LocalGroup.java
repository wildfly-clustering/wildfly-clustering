/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local;

import org.wildfly.clustering.server.GroupMembershipListener;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.group.Group;

/**
 * Local view of the Group.
 * @author Paul Ferraro
 */
public interface LocalGroup extends Group<String, LocalGroupMember> {

	@Override
	LocalGroupMemberFactory getGroupMemberFactory();

	@Override
	default boolean isSingleton() {
		return true;
	}

	@Override
	default Registration register(GroupMembershipListener<LocalGroupMember> object) {
		return Registration.EMPTY;
	}

	/**
	 * Creates a local group with the specified group and member names.
	 * @param groupName a group name
	 * @param memberName a member name
	 * @return a local group with the specified group and member names.
	 */
	static LocalGroup of(String groupName, String memberName) {
		return new DefaultLocalGroup(groupName, memberName);
	}
}
