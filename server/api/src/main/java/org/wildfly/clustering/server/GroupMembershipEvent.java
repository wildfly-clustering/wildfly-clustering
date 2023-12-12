/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server;

/**
 * @author Paul Ferraro
 */
public interface GroupMembershipEvent<M extends GroupMember> {

	GroupMembership<M> getPreviousMembership();

	GroupMembership<M> getCurrentMembership();
}
