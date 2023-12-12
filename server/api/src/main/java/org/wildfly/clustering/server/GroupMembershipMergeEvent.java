/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server;

import java.util.List;

/**
 * @author Paul Ferraro
 */
public interface GroupMembershipMergeEvent<M extends GroupMember> extends GroupMembershipEvent<M> {

	List<? extends GroupMembership<M>> getPartitions();
}
