/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.group;

import org.wildfly.clustering.server.GroupMember;

/**
 * @author Paul Ferraro
 */
public interface GroupMembership<M extends GroupMember> extends org.wildfly.clustering.server.GroupMembership<M> {

	int getCoordinatorIndex();

	@Override
	default M getCoordinator() {
		return this.getMembers().get(this.getCoordinatorIndex());
	}
}
