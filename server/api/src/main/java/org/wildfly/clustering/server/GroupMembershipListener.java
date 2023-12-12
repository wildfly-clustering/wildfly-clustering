/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server;

/**
 * Listener for {@link Group} membership changes.
 * @author Paul Ferraro
 */
public interface GroupMembershipListener<M extends GroupMember> {
	/**
	 * Indicates that the membership of the group has changed.
	 *
	 * @param event the membership event
	 */
	void updated(GroupMembershipEvent<M> event);

	/**
	 * Indicates that the membership of the group has changed, probably due to a network partition.
	 *
	 * @param event the membership event
	 */
	default void split(GroupMembershipEvent<M> event) {
		this.updated(event);
	}

	/**
	 * Indicates that the membership of the group has changed as the result of a network partition merge.
	 *
	 * @param event the membership event
	 */
	default void merged(GroupMembershipMergeEvent<M> event) {
		this.updated(event);
	}
}
