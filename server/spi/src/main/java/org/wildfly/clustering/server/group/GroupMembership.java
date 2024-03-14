/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.group;

import org.wildfly.clustering.server.GroupMember;

/**
 * Group membership whose coordinator is identified by its index in the membership.
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public interface GroupMembership<M extends GroupMember> extends org.wildfly.clustering.server.GroupMembership<M> {

	int getCoordinatorIndex();

	@Override
	default M getCoordinator() {
		return this.getMembers().get(this.getCoordinatorIndex());
	}
}
