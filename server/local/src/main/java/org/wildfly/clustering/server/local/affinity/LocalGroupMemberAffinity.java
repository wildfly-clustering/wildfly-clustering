/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.affinity;

import java.util.function.Function;

import org.wildfly.clustering.server.Group;
import org.wildfly.clustering.server.GroupMember;

/**
 * An affinity function that always returns the local group member.
 * @param <I> the identifier type for group members
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public class LocalGroupMemberAffinity<I, M extends GroupMember> implements Function<I, M> {

	private final M member;

	/**
	 * Creates an affinity function that always returns the local group member.
	 * @param group a group
	 */
	public LocalGroupMemberAffinity(Group<M> group) {
		this(group.getLocalMember());
	}

	LocalGroupMemberAffinity(M member) {
		this.member = member;
	}

	@Override
	public M apply(I t) {
		return this.member;
	}
}
