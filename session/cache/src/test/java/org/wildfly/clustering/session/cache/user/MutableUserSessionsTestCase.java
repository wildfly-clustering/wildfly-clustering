/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.session.user.UserSessions;

/**
 * Unit test for {@link MutableUserSessions}.
 * @author Paul Ferraro
 */
public class MutableUserSessionsTestCase {

	@Test
	public void getApplications() {
		UUID key = UUID.randomUUID();
		CacheEntryMutatorFactory<UUID, Map<String, String>> mutatorFactory = mock(CacheEntryMutatorFactory.class);
		Map<String, String> deployments = Map.of("deployment1", "session1", "deployment2", "session2");
		try (UserSessions<String, String> sessions = new MutableUserSessions<>(key, deployments, mutatorFactory)) {
			assertThat(sessions.getDeployments()).containsExactlyInAnyOrder("deployment1", "deployment2");
		}

		verifyNoInteractions(mutatorFactory);
	}

	@Test
	public void getSession() {
		UUID key = UUID.randomUUID();
		CacheEntryMutatorFactory<UUID, Map<String, String>> mutatorFactory = mock(CacheEntryMutatorFactory.class);
		Map<String, String> deployments = Map.of("deployment1", "session1", "deployment2", "session2");
		try (UserSessions<String, String> sessions = new MutableUserSessions<>(key, deployments, mutatorFactory)) {
			assertThat(sessions.getSession("deployment1")).isEqualTo("session1");
			assertThat(sessions.getSession("deployment2")).isEqualTo("session2");
			assertThat(sessions.getSession("foo")).isNull();
		}

		verifyNoInteractions(mutatorFactory);
	}

	@Test
	public void addSession() {
		UUID key = UUID.randomUUID();
		CacheEntryMutatorFactory<UUID, Map<String, String>> mutatorFactory = mock(CacheEntryMutatorFactory.class);
		CacheEntryMutator mutator = mock(CacheEntryMutator.class);

		doReturn(mutator).when(mutatorFactory).createMutator(key, Map.of("deployment2", "session2"));

		Map<String, String> deployments = new TreeMap<>();
		deployments.put("deployment1", "session1");
		try (UserSessions<String, String> sessions = new MutableUserSessions<>(key, deployments, mutatorFactory)) {
			assertThat(sessions.addSession("deployment1", "session3")).isFalse();
			assertThat(sessions.addSession("deployment2", "session2")).isTrue();

			verifyNoInteractions(mutatorFactory);
		}

		verify(mutator).run();
	}

	@Test
	public void removeSession() {
		UUID key = UUID.randomUUID();
		CacheEntryMutatorFactory<UUID, Map<String, String>> mutatorFactory = mock(CacheEntryMutatorFactory.class);
		CacheEntryMutator mutator = mock(CacheEntryMutator.class);

		Map<String, String> updates = new TreeMap<>();
		updates.put("deployment1", null);
		doReturn(mutator).when(mutatorFactory).createMutator(key, updates);

		Map<String, String> deployments = new TreeMap<>();
		deployments.put("deployment1", "session1");
		deployments.put("deployment2", "session2");
		try (UserSessions<String, String> sessions = new MutableUserSessions<>(key, deployments, mutatorFactory)) {
			assertThat(sessions.removeSession("deployment1")).isEqualTo("session1");
			assertThat(sessions.removeSession("deployment3")).isNull();

			verifyNoInteractions(mutatorFactory);
		}

		verify(mutator).run();
	}
}
