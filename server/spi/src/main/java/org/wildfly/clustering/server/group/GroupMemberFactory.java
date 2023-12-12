/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.group;

/**
 * A factory for creating a group member.
 * @author Paul Ferraro
 */
public interface GroupMemberFactory<A extends Comparable<A>, M extends GroupMember<A>> {

	/**
	 * Creates a group member with the specified identifier.
	 * @param address the unique identifier of the group member
	 * @return a group member.
	 */
	M createGroupMember(A address);
}
