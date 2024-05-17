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
 * Unit test for {@link LocalServiceProviderRegistrar}.
 * @author Paul Ferraro
 */
public class LocalServiceProviderRegistrarTestCase {

	@Test
	public void test() {
		LocalGroup group = mock(LocalGroup.class);
		LocalGroupMember localMember = mock(LocalGroupMember.class);

		doReturn(localMember).when(group).getLocalMember();

		LocalServiceProviderRegistrar<String, LocalGroupMember> registrar = new LocalServiceProviderRegistrar<>(group);

		Assertions.assertSame(group, registrar.getGroup());

		Assertions.assertEquals(Set.of(), registrar.getServices());
		Assertions.assertEquals(Set.of(), registrar.getProviders("foo"));
		Assertions.assertEquals(Set.of(), registrar.getProviders("bar"));

		ServiceProviderRegistration<String, LocalGroupMember> foo = registrar.register("foo");

		Assertions.assertEquals("foo", foo.getService());
		Assertions.assertEquals(Set.of(localMember), foo.getProviders());

		Assertions.assertEquals(Set.of("foo"), registrar.getServices());
		Assertions.assertEquals(Set.of(localMember), registrar.getProviders("foo"));
		Assertions.assertEquals(Set.of(), registrar.getProviders("bar"));

		ServiceProviderRegistration<String, LocalGroupMember> bar = registrar.register("bar");

		Assertions.assertEquals("bar", bar.getService());
		Assertions.assertEquals(Set.of(localMember), bar.getProviders());

		Assertions.assertEquals(Set.of("foo", "bar"), registrar.getServices());
		Assertions.assertEquals(Set.of(localMember), registrar.getProviders("foo"));
		Assertions.assertEquals(Set.of(localMember), registrar.getProviders("bar"));

		foo.close();

		Assertions.assertEquals(Set.of("bar"), registrar.getServices());
		Assertions.assertEquals(Set.of(), registrar.getProviders("foo"));
		Assertions.assertEquals(Set.of(localMember), registrar.getProviders("bar"));

		bar.close();

		Assertions.assertEquals(Set.of(), registrar.getServices());
		Assertions.assertEquals(Set.of(), registrar.getProviders("foo"));
		Assertions.assertEquals(Set.of(), registrar.getProviders("bar"));
	}
}
