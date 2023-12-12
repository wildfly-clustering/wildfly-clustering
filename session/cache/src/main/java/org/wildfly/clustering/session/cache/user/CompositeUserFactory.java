/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import java.util.Map;

import org.wildfly.clustering.session.user.User;
import org.wildfly.clustering.session.user.UserSessions;

/**
 * @author Paul Ferraro
 */
public class CompositeUserFactory<CV, C, L, SV, D, S> implements UserFactory<CV, C, L, SV, D, S> {

	private final UserContextFactory<CV, C, L> contextFactory;
	private final UserSessionsFactory<SV, D, S> sessionsFactory;

	public CompositeUserFactory(UserContextFactory<CV, C, L> contextFactory, UserSessionsFactory<SV, D, S> sessionsFactory) {
		this.contextFactory = contextFactory;
		this.sessionsFactory = sessionsFactory;
	}

	@Override
	public User<C, L, D, S> createUser(String id, Map.Entry<CV, SV> entry) {
		CV contextValue = entry.getKey();
		SV sessionsValue = entry.getValue();
		if ((contextValue == null) || (sessionsValue == null)) return null;
		Map.Entry<C, L> context = this.contextFactory.createUserContext(contextValue);
		UserSessions<D, S> sessions = this.sessionsFactory.createUserSessions(id, sessionsValue);
		return new CompositeUser<>(id, context, sessions, this);
	}

	@Override
	public UserContextFactory<CV, C, L> getUserContextFactory() {
		return this.contextFactory;
	}

	@Override
	public UserSessionsFactory<SV, D, S> getUserSessionsFactory() {
		return this.sessionsFactory;
	}
}
