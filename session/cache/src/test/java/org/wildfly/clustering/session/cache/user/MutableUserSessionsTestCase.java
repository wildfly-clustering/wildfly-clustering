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
import org.mockito.ArgumentCaptor;
import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.server.util.MapEntry;
import org.wildfly.clustering.session.user.UserSessions;

/**
 * Unit test for {@link MutableUserSessions}.
 * @author Paul Ferraro
 */
public class MutableUserSessionsTestCase {

	@Test
	public void getSessions() {
		UUID key = UUID.randomUUID();
		CacheEntryMutatorFactory<UUID, Map<String, String>> mutatorFactory = mock(CacheEntryMutatorFactory.class);
		Map<String, String> deployments = new TreeMap<>(Map.of("deployment1", "session1", "deployment2", "session2"));
		try (UserSessions<String, String> sessions = new MutableUserSessions<>(key, deployments, mutatorFactory)) {
			assertThat(sessions.getSessions()).containsExactlyInAnyOrderEntriesOf(deployments);
		}

		verifyNoInteractions(mutatorFactory);
	}

	@Test
	public void getSession() {
		UUID key = UUID.randomUUID();
		CacheEntryMutatorFactory<UUID, Map<String, String>> mutatorFactory = mock(CacheEntryMutatorFactory.class);
		Map<String, String> deployments = new TreeMap<>(Map.of("deployment1", "session1", "deployment2", "session2"));
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

		ArgumentCaptor<Map<String, String>> capturedUpdates = ArgumentCaptor.captor();

		doReturn(mutator).when(mutatorFactory).createMutator(same(key), capturedUpdates.capture());

		Map<String, String> deployments = new TreeMap<>();
		deployments.put("existingDeployment", "existingSession");

		try (UserSessions<String, String> sessions = new MutableUserSessions<>(key, deployments, mutatorFactory)) {
			assertThat(sessions.addSession("existingDeployment", "otherSession")).isFalse();
			assertThat(sessions.addSession("newDeployment", "newSession")).isTrue();

			verifyNoInteractions(mutatorFactory);
		}

		verify(mutator).run();

		assertThat(capturedUpdates.getValue()).containsOnly(Map.entry("newDeployment", "newSession"));
	}

	@Test
	public void removeSession() {
		UUID key = UUID.randomUUID();
		CacheEntryMutatorFactory<UUID, Map<String, String>> mutatorFactory = mock(CacheEntryMutatorFactory.class);
		CacheEntryMutator mutator = mock(CacheEntryMutator.class);

		ArgumentCaptor<Map<String, String>> capturedUpdates = ArgumentCaptor.captor();

		doReturn(mutator).when(mutatorFactory).createMutator(same(key), capturedUpdates.capture());

		Map<String, String> deployments = new TreeMap<>();
		deployments.put("removedDeployment", "removedSession");
		deployments.put("remainingDeployment", "remainingSession");

		try (UserSessions<String, String> sessions = new MutableUserSessions<>(key, deployments, mutatorFactory)) {
			assertThat(sessions.removeSession("removedDeployment")).isEqualTo("removedSession");
			assertThat(sessions.removeSession("missingDeployment")).isNull();

			verifyNoInteractions(mutatorFactory);
		}

		verify(mutator).run();

		assertThat(capturedUpdates.getValue()).containsOnly(MapEntry.of("removedDeployment", null));
	}
}
