/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.affinity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.GroupMember;

/**
 * @author Paul Ferraro
 */
public class NarySessionAffinityTestCase {

	@Test
	public void test() {
		GroupMember primary = mock(GroupMember.class);
		GroupMember backup = mock(GroupMember.class);
		GroupMember local = mock(GroupMember.class);

		Map<String, List<GroupMember>> affinity = Map.of("session", List.of(primary, backup, local));
		Map<GroupMember, String> mapping = Map.of(primary, "foo", backup, "bar", local, "qux");
		UnaryOperator<String> sessionAffinity = new NarySessionAffinity<>(affinity::get, mapping::get, () -> "#");

		assertEquals("foo#bar#qux", sessionAffinity.apply("session"));
	}

	@Test
	public void testDupes() {
		GroupMember primary = mock(GroupMember.class);
		GroupMember backup = mock(GroupMember.class);
		GroupMember local = mock(GroupMember.class);

		Map<String, List<GroupMember>> affinity = Map.of("session", List.of(primary, backup, local));
		Map<GroupMember, String> mapping = Map.of(primary, "foo", backup, "bar", local, "bar");
		UnaryOperator<String> sessionAffinity = new NarySessionAffinity<>(affinity::get, mapping::get, () -> "-");

		assertEquals("foo-bar", sessionAffinity.apply("session"));
	}

	@Test
	public void testLimit() {
		GroupMember primary = mock(GroupMember.class);
		GroupMember backup = mock(GroupMember.class);
		GroupMember local = mock(GroupMember.class);

		Map<String, List<GroupMember>> affinity = Map.of("session", List.of(primary, backup, local));
		Map<GroupMember, String> mapping = Map.of(primary, "foo", backup, "bar", local, "qux");
		UnaryOperator<String> sessionAffinity = new NarySessionAffinity<>(affinity::get, mapping::get, new NarySessionAffinityConfiguration() {
			@Override
			public String getDelimiter() {
				return ",";
			}

			@Override
			public int getMaxMembers() {
				return 2;
			}
		});

		assertEquals("foo,bar", sessionAffinity.apply("session"));
	}
}
