/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.registry;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.function.BiFunction;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.GroupMember;

/**
 * Unit test for {@link RegistryFactory#singleton(BiFunction)}.
 *
 * @author Radoslav Husar
 */
public class RegistryFactoryTestCase {

	@Test
	public void singletonCreationFailure() {
		Map.Entry<String, String> entry = Map.entry("foo", "bar");
		Registry<GroupMember, String, String> registry = mock(Registry.class);
		RuntimeException exception = new RuntimeException();
		BiFunction<Map.Entry<String, String>, Runnable, Registry<GroupMember, String, String>> factory = mock(BiFunction.class);
		RegistryFactory<GroupMember, String, String> registryFactory = RegistryFactory.singleton(factory);

		doThrow(exception).when(factory).apply(same(entry), any());

		assertThatThrownBy(() -> registryFactory.createRegistry(entry)).isSameAs(exception);

		// A single failed creation attempt cannot prevent subsequent attempts
		doReturn(registry).when(factory).apply(same(entry), any());

		assertThat(registryFactory.createRegistry(entry)).isSameAs(registry);
	}
}
