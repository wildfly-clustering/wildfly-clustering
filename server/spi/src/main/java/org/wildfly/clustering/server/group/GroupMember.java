/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.group;

/**
 * Exposes the unique identifier of a {@link org.wildfly.clustering.server.GroupMember}.
 * @author Paul Ferraro
 * @param <I> the group member identifier type
 */
public interface GroupMember<I extends Comparable<I>> extends org.wildfly.clustering.server.GroupMember, Comparable<GroupMember<I>> {

	/**
	 * Returns the unique address of this group member
	 * @return the unique address of this group member
	 */
	I getId();
}
