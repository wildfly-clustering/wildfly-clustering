/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local;

import static org.assertj.core.api.Assertions.*;

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

		assertThat(group.getName()).isSameAs(GROUP_NAME);
		assertThat(group.getLocalMember().getName()).isSameAs(MEMBER_NAME);
		assertThat(group.getMembership().getMembers()).containsExactly(group.getLocalMember());
		assertThat(group.getMembership().getCoordinator()).isSameAs(group.getLocalMember());
	}
}
