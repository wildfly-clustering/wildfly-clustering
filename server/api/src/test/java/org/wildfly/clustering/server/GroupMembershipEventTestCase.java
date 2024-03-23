/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Validates the default methods of a {@link GroupMembershipEvent}.
 * @author Paul Ferraro
 */
public class GroupMembershipEventTestCase {

	@Test
	public void test() {
		GroupMember joined = mock(GroupMember.class);
		GroupMember left = mock(GroupMember.class);
		GroupMember existing = mock(GroupMember.class);

		GroupMembership<GroupMember> previous = mock(GroupMembership.class);
		GroupMembership<GroupMember> current = mock(GroupMembership.class);

		doReturn(List.of(left, existing)).when(previous).getMembers();
		doReturn(List.of(existing, joined)).when(current).getMembers();

		GroupMembershipEvent<GroupMember> event = new GroupMembershipEvent<>() {
			@Override
			public GroupMembership<GroupMember> getPreviousMembership() {
				return previous;
			}

			@Override
			public GroupMembership<GroupMember> getCurrentMembership() {
				return current;
			}
		};

		Assertions.assertEquals(Set.of(joined), event.getJoiners());
		Assertions.assertEquals(Set.of(left), event.getLeavers());
	}
}
