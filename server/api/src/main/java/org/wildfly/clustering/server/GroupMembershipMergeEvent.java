/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server;

import java.util.List;

/**
 * Encapsulates a {@link GroupMembership} merge event.
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public interface GroupMembershipMergeEvent<M extends GroupMember> extends GroupMembershipEvent<M> {

	/**
	 * Returns the group memberships that were merged.
	 * @return a list of group memberships.
	 */
	List<GroupMembership<M>> getPartitions();
}
