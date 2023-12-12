/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.group;

/**
 * Exposes the unique identifier of a {@link org.wildfly.clustering.server.GroupMember}.
 * @author Paul Ferraro
 * @param <A> the group member identifier type
 */
public interface GroupMember<A extends Comparable<A>> extends org.wildfly.clustering.server.GroupMember, Comparable<GroupMember<A>> {

	/**
	 * Returns the unique identifier of this group member
	 * @return a unique identifier
	 */
	A getAddress();

	@Override
	default int compareTo(GroupMember<A> member) {
		return this.getAddress().compareTo(member.getAddress());
	}
}
