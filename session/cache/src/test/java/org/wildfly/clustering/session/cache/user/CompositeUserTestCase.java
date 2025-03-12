/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.CacheEntryRemover;
import org.wildfly.clustering.session.user.User;
import org.wildfly.clustering.session.user.UserSessions;

public class CompositeUserTestCase {
	private final String id = "id";
	private final UserSessions<String, String> sessions = mock(UserSessions.class);
	private final String context = "context";
	private final String localContext = "local-context";
	private final CacheEntryRemover<String> remover = mock(CacheEntryRemover.class);

	private final User<String, Object, String, String> user = new CompositeUser<>(this.id, Map.entry(this.context, this.localContext), this.sessions, this.remover);

	@Test
	public void getId() {
		assertThat(this.user.getId()).isSameAs(this.id);
	}

	@Test
	public void getContext() {
		assertThat(this.user.getPersistentContext()).isSameAs(this.context);
	}

	@Test
	public void getSessions() {
		assertThat(this.user.getSessions()).isSameAs(this.sessions);
	}

	@Test
	public void invalidate() {
		this.user.invalidate();

		verify(this.remover).remove(this.id);
	}

	@Test
	public void getLocalContext() {
		assertThat(this.user.getTransientContext()).isSameAs(this.localContext);
	}
}
