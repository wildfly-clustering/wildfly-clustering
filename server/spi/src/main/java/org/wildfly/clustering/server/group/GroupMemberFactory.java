/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.group;

/**
 * A factory for creating a group member.
 * @param <I> the group member identifier type
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public interface GroupMemberFactory<I extends Comparable<I>, M extends GroupMember<I>> {

	/**
	 * Creates a group member with the specified identifier.
	 * @param id the unique identifier of the group member
	 * @return a group member.
	 */
	M createGroupMember(I id);
}
