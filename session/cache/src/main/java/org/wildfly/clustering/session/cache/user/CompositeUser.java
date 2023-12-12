/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache.user;

import java.util.Map;

import org.wildfly.clustering.cache.Remover;
import org.wildfly.clustering.session.user.User;
import org.wildfly.clustering.session.user.UserSessions;

public class CompositeUser<C, L, D, S> implements User<C, L, D, S> {
	private final String id;
	private final Map.Entry<C, L> contextEntry;
	private final UserSessions<D, S> sessions;
	private final Remover<String> remover;

	public CompositeUser(String id, Map.Entry<C, L> contextEntry, UserSessions<D, S> sessions, Remover<String> remover) {
		this.id = id;
		this.contextEntry = contextEntry;
		this.sessions = sessions;
		this.remover = remover;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public C getContext() {
		return this.contextEntry.getKey();
	}

	@Override
	public UserSessions<D, S> getSessions() {
		return this.sessions;
	}

	@Override
	public void invalidate() {
		this.remover.remove(this.id);
	}

	@Override
	public L getLocalContext() {
		return this.contextEntry.getValue();
	}
}
