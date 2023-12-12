/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * @author Paul Ferraro
 */
public class LocalGroupTestCase {

	private static final String GROUP_NAME = "group";
	private static final String MEMBER_NAME = "member";

	@Test
	public void test() {
		LocalGroup group = new DefaultLocalGroup(GROUP_NAME, MEMBER_NAME);

		assertSame(GROUP_NAME, group.getName());
		assertSame(MEMBER_NAME, group.getLocalMember().getName());
		assertEquals(List.of(group.getLocalMember()), group.getMembership().getMembers());
		assertSame(group.getLocalMember(), group.getMembership().getCoordinator());
	}
}
