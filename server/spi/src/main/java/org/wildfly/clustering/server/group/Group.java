/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.group;

/**
 * Exposes a mechanism to create a group member for a given unique address.
 * @author Paul Ferraro
 * @param <A> the group member address type
 */
public interface Group<A extends Comparable<A>, M extends GroupMember<A>> extends org.wildfly.clustering.server.Group<M> {

	GroupMemberFactory<A, M> getGroupMemberFactory();
}
