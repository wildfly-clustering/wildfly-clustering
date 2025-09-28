/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.group;

/**
 * Exposes a mechanism to create a group member for a given unique address.
 * @param <I> the group member address type
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public interface Group<I extends Comparable<I>, M extends GroupMember<I>> extends org.wildfly.clustering.server.Group<M> {

	/**
	 * Returns a factory for creating group members from an identifiers.
	 * @return a group member factory
	 */
	GroupMemberFactory<I, M> getGroupMemberFactory();
}
