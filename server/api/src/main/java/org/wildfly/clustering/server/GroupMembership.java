/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server;

import java.util.List;

/**
 * Encapsulates the membership of a group.
 * @author Paul Ferraro
 * @param <M> the member type
 */
public interface GroupMembership<M extends GroupMember> {

	/**
	 * Returns the coordinator of this group membership.
	 * All members of this membership will always agree on which member is the coordinator.
	 *
	 * @return the group coordinator
	 */
	M getCoordinator();

	/**
	 * Returns the members comprising this group membership.
	 * The membership order will be consistent on each member in the group.
	 *
	 * @return an immutable list of members ordered by descending age.
	 */
	List<M> getMembers();
}
