/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.provider;

import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.local.LocalGroup;
import org.wildfly.clustering.server.local.LocalGroupMember;
import org.wildfly.clustering.server.provider.ServiceProviderRegistration;

/**
 * Unit test for {@link LocalServiceProviderRegistry}.
 * @author Paul Ferraro
 */
public class LocalServiceProviderRegistryTestCase {

	@Test
	public void test() {
		LocalGroup group = mock(LocalGroup.class);
		LocalGroupMember localMember = mock(LocalGroupMember.class);

		doReturn(localMember).when(group).getLocalMember();

		LocalServiceProviderRegistry<String, LocalGroupMember> registry = new LocalServiceProviderRegistry<>(group);

		Assertions.assertSame(group, registry.getGroup());

		Assertions.assertEquals(Set.of(), registry.getServices());
		Assertions.assertEquals(Set.of(), registry.getProviders("foo"));
		Assertions.assertEquals(Set.of(), registry.getProviders("bar"));

		ServiceProviderRegistration<String, LocalGroupMember> foo = registry.register("foo");

		Assertions.assertEquals("foo", foo.getService());
		Assertions.assertEquals(Set.of(localMember), foo.getProviders());

		Assertions.assertEquals(Set.of("foo"), registry.getServices());
		Assertions.assertEquals(Set.of(localMember), registry.getProviders("foo"));
		Assertions.assertEquals(Set.of(), registry.getProviders("bar"));

		ServiceProviderRegistration<String, LocalGroupMember> bar = registry.register("bar");

		Assertions.assertEquals("bar", bar.getService());
		Assertions.assertEquals(Set.of(localMember), bar.getProviders());

		Assertions.assertEquals(Set.of("foo", "bar"), registry.getServices());
		Assertions.assertEquals(Set.of(localMember), registry.getProviders("foo"));
		Assertions.assertEquals(Set.of(localMember), registry.getProviders("bar"));

		foo.close();

		Assertions.assertEquals(Set.of("bar"), registry.getServices());
		Assertions.assertEquals(Set.of(), registry.getProviders("foo"));
		Assertions.assertEquals(Set.of(localMember), registry.getProviders("bar"));

		bar.close();

		Assertions.assertEquals(Set.of(), registry.getServices());
		Assertions.assertEquals(Set.of(), registry.getProviders("foo"));
		Assertions.assertEquals(Set.of(), registry.getProviders("bar"));
	}
}
