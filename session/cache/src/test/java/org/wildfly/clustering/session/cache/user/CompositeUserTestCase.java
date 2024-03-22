/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache.user;

import static org.junit.jupiter.api.Assertions.*;
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
		assertSame(this.id, this.user.getId());
	}

	@Test
	public void getContext() {
		assertSame(this.context, this.user.getPersistentContext());
	}

	@Test
	public void getSessions() {
		assertSame(this.sessions, this.user.getSessions());
	}

	@Test
	public void invalidate() {
		this.user.invalidate();

		verify(this.remover).remove(this.id);
	}

	@Test
	public void getLocalContext() {
		assertSame(this.localContext, this.user.getTransientContext());
	}
}
