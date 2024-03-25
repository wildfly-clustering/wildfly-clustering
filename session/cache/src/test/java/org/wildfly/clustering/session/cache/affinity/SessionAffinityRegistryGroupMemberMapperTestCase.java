/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.affinity;

import static org.mockito.Mockito.*;

import java.util.AbstractMap;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.Group;
import org.wildfly.clustering.server.GroupMember;
import org.wildfly.clustering.server.registry.Registry;

/**
 * @author Paul Ferraro
 */
public class SessionAffinityRegistryGroupMemberMapperTestCase {

	@Test
	public void test() {
		Registry<GroupMember, String, Void> registry = mock(Registry.class);
		Group<GroupMember> group = mock(Group.class);
		GroupMember localMember = mock(GroupMember.class);
		GroupMember remoteMember = mock(GroupMember.class);
		GroupMember leftMember = mock(GroupMember.class);
		String localAffinity = "foo";
		String remoteAffinity = "bar";

		doReturn(group).when(registry).getGroup();
		doReturn(localMember).when(group).getLocalMember();
		doReturn(new AbstractMap.SimpleImmutableEntry<>(localAffinity, null)).when(registry).getEntry(localMember);
		doReturn(new AbstractMap.SimpleImmutableEntry<>(remoteAffinity, null)).when(registry).getEntry(remoteMember);

		Function<GroupMember, String> mapper = new SessionAffinityRegistryGroupMemberMapper<>(registry);

		Assertions.assertSame(localAffinity, mapper.apply(null));
		Assertions.assertSame(localAffinity, mapper.apply(localMember));
		Assertions.assertSame(localAffinity, mapper.apply(leftMember));
		Assertions.assertSame(remoteAffinity, mapper.apply(remoteMember));
	}
}
