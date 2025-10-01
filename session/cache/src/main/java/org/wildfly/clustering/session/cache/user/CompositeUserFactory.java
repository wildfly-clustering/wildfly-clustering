/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import java.util.Map;

import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.session.user.User;
import org.wildfly.clustering.session.user.UserSessions;

/**
 * A user factory that creates composite users.
 * @param <CV> the user context value type
 * @param <C> the persistent context type
 * @param <T> the transient context type
 * @param <SV> the user sessions value type
 * @param <D> the deployment type
 * @param <S> the session type
 */
public class CompositeUserFactory<CV, C, T, SV, D, S> implements UserFactory<CV, C, T, SV, D, S> {

	private final UserContextFactory<CV, C, T> contextFactory;
	private final UserSessionsFactory<SV, D, S> sessionsFactory;
	private final CacheProperties properties;

	/**
	 * Creates a factory for creating a composite user.
	 * @param contextFactory a context factory
	 * @param sessionsFactory a factory for creating user sessions
	 * @param properties the properties of the associated cache
	 */
	public CompositeUserFactory(UserContextFactory<CV, C, T> contextFactory, UserSessionsFactory<SV, D, S> sessionsFactory, CacheProperties properties) {
		this.contextFactory = contextFactory;
		this.sessionsFactory = sessionsFactory;
		this.properties = properties;
	}

	@Override
	public User<C, T, D, S> createUser(String id, Map.Entry<CV, SV> entry) {
		CV contextValue = entry.getKey();
		SV sessionsValue = entry.getValue();
		if ((contextValue == null) || (sessionsValue == null)) return null;
		Map.Entry<C, T> context = this.contextFactory.createUserContext(contextValue);
		UserSessions<D, S> sessions = this.sessionsFactory.createUserSessions(id, sessionsValue);
		return new CompositeUser<>(id, context, sessions, this);
	}

	@Override
	public UserContextFactory<CV, C, T> getUserContextFactory() {
		return this.contextFactory;
	}

	@Override
	public UserSessionsFactory<SV, D, S> getUserSessionsFactory() {
		return this.sessionsFactory;
	}

	@Override
	public CacheProperties getCacheProperties() {
		return this.properties;
	}
}
