/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.affinity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.GroupMember;

/**
 * @author Paul Ferraro
 */
public class UnarySessionAffinityTestCase {

	@Test
	public void test() {
		test(null);
		test("affinity");
	}

	static void test(String expected) {
		GroupMember foo = mock(GroupMember.class);
		GroupMember bar = mock(GroupMember.class);
		Map<String, GroupMember> affinity = Map.of("foo", foo, "bar", bar);
		Map<GroupMember, String> mapping = Map.of(foo, "foo-server", bar, "bar-server");
		UnaryOperator<String> sessionAffinity = new UnarySessionAffinity<>(affinity::get, mapping::get);
		assertEquals("foo-server", sessionAffinity.apply("foo"));
		assertEquals("bar-server", sessionAffinity.apply("bar"));
	}
}
