/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.group;

/**
 * An abstract implementation of a group member.
 * @author Paul Ferraro
 * @param <I> the group member identifier
 */
public abstract class AbstractGroupMember<I extends Comparable<I>> implements GroupMember<I> {

	/**
	 * Creates a group member.
	 */
	protected AbstractGroupMember() {
	}

	@Override
	public int compareTo(GroupMember<I> member) {
		return this.getId().compareTo(member.getId());
	}

	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof org.wildfly.clustering.server.group.GroupMember member)) return false;
		return this.getId().equals(member.getId());
	}

	@Override
	public String toString() {
		return this.getName();
	}
}
