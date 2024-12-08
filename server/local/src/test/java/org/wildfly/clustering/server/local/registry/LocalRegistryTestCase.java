/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.registry;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.local.LocalGroup;
import org.wildfly.clustering.server.local.LocalGroupMember;

/**
 * Unit test for {@link LocalRegistry}.
 * @author Paul Ferraro
 */
public class LocalRegistryTestCase {

	@Test
	public void test() {
		LocalGroup group = mock(LocalGroup.class);
		LocalGroupMember localMember = mock(LocalGroupMember.class);
		LocalGroupMember nonMember = mock(LocalGroupMember.class);
		Runnable closeTask = mock(Runnable.class);
		Map.Entry<String, String> entry = Map.entry("foo", "bar");

		doReturn(localMember).when(group).getLocalMember();

		LocalRegistry<String, String> registry = LocalRegistry.of(group, entry, closeTask);

		assertThat(registry.getGroup()).isSameAs(group);
		assertThat(registry.getEntry(localMember)).isSameAs(entry);
		assertThat(registry.getEntry(nonMember)).isNull();
		assertThat(registry.getEntries()).containsExactly(entry);

		verifyNoInteractions(closeTask);

		registry.close();

		verify(closeTask).run();

		Assertions.assertNull(registry.getEntry(localMember));
		Assertions.assertNull(registry.getEntry(nonMember));
		Assertions.assertEquals(Map.of(), registry.getEntries());

		registry.close();

		verifyNoMoreInteractions(closeTask);
	}
}
