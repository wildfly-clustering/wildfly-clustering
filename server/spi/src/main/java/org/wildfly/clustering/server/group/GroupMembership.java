/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.group;

import java.util.List;

import org.wildfly.clustering.server.GroupMember;

/**
 * Group membership whose coordinator is identified by its index in the membership.
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public interface GroupMembership<M extends GroupMember> extends org.wildfly.clustering.server.GroupMembership<M> {

	/**
	 * Returns the index of the group coordinator.
	 * @return the index of the group coordinator.
	 */
	int getCoordinatorIndex();

	@Override
	default M getCoordinator() {
		return this.getMembers().get(this.getCoordinatorIndex());
	}

	/**
	 * Returns a singleton group membership.
	 * @param <M> the member type
	 * @param member the single group member
	 * @return a singleton group membership.
	 */
	static <M extends GroupMember> GroupMembership<M> singleton(M member) {
		List<M> members = List.of(member);
		return new GroupMembership<>() {
			@Override
			public int getCoordinatorIndex() {
				return 0;
			}

			@Override
			public List<M> getMembers() {
				return members;
			}

			@Override
			public int hashCode() {
				return this.getCoordinator().hashCode();
			}

			@Override
			public boolean equals(Object object) {
				if (!(object instanceof GroupMembership membership)) return false;
				return members.equals(membership.getMembers());
			}

			@Override
			public String toString() {
				return members.toString();
			}
		};
	}
}
