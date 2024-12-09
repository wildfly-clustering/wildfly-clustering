/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.provider;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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

		LocalServiceProviderRegistrar<String> registrar = LocalServiceProviderRegistrar.of(group);

		assertThat(registrar.getGroup()).isSameAs(group);

		assertThat(registrar.getServices()).isEmpty();
		assertThat(registrar.getProviders("foo")).isEmpty();
		assertThat(registrar.getProviders("bar")).isEmpty();

		ServiceProviderRegistration<String, LocalGroupMember> foo = registrar.register("foo");

		assertThat(foo.getService()).isEqualTo("foo");
		assertThat(foo.getProviders()).containsExactly(localMember);

		assertThat(registrar.getServices()).containsExactly("foo");
		assertThat(registrar.getProviders("foo")).containsExactly(localMember);
		assertThat(registrar.getProviders("bar")).isEmpty();

		ServiceProviderRegistration<String, LocalGroupMember> bar = registrar.register("bar");

		assertThat(bar.getService()).isEqualTo("bar");
		assertThat(bar.getProviders()).containsExactly(localMember);

		assertThat(registrar.getServices()).containsExactlyInAnyOrder("foo", "bar");
		assertThat(registrar.getProviders("foo")).containsExactly(localMember);
		assertThat(registrar.getProviders("bar")).containsExactly(localMember);

		foo.close();

		assertThat(registrar.getServices()).containsExactly("bar");
		assertThat(registrar.getProviders("foo")).isEmpty();
		assertThat(registrar.getProviders("bar")).containsExactly(localMember);

		bar.close();

		assertThat(registrar.getServices()).isEmpty();
		assertThat(registrar.getProviders("foo")).isEmpty();
		assertThat(registrar.getProviders("bar")).isEmpty();
	}
}
