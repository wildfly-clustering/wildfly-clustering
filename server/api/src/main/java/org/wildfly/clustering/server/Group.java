/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server;

/**
 * Represents a set of group members.
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public interface Group<M extends GroupMember> extends Registrar<GroupMembershipListener<M>> {

	/**
	 * Returns the logical name of this group.
	 *
	 * @return the group name
	 */
	String getName();

	/**
	 * Returns the local member.
	 *
	 * @return the local member
	 */
	M getLocalMember();

	/**
	 * Gets the current membership of this group
	 * @return the group membership
	 */
	GroupMembership<M> getMembership();

	/**
	 * Indicates whether or not this is a singleton group.  The membership of a singleton group contains only the local member and never changes.
	 * @return true, if this is a singleton group, false otherwise.
	 */
	boolean isSingleton();
}
