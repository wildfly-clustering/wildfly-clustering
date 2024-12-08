/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

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

		assertThat(event.getJoiners()).containsExactly(joined);
		assertThat(event.getLeavers()).containsExactly(left);
	}
}
