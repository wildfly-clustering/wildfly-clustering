/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates a {@link GroupMembership} event.
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public interface GroupMembershipEvent<M extends GroupMember> {

	GroupMembership<M> getPreviousMembership();

	GroupMembership<M> getCurrentMembership();

	default Set<M> getLeavers() {
		Set<M> members = new HashSet<>(this.getPreviousMembership().getMembers());
		members.removeAll(this.getCurrentMembership().getMembers());
		return Collections.unmodifiableSet(members);
	}

	default Set<M> getJoiners() {
		Set<M> members = new HashSet<>(this.getCurrentMembership().getMembers());
		members.removeAll(this.getPreviousMembership().getMembers());
		return Collections.unmodifiableSet(members);
	}
}
