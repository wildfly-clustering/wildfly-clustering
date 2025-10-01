/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import org.wildfly.clustering.cache.CacheEntryCreator;
import org.wildfly.clustering.cache.CacheEntryLocator;
import org.wildfly.clustering.cache.CacheEntryRemover;
import org.wildfly.clustering.session.user.UserSessions;

/**
 * A factory for creating the sessions for a user.
 * @author Paul Ferraro
 * @param <V> the cache value type
 * @param <D> the deployment identifier type
 * @param <S> the session identifier type
 */
public interface UserSessionsFactory<V, D, S> extends CacheEntryCreator<String, V, Void>, CacheEntryLocator<String, V>, CacheEntryRemover<String> {
	/**
	 * Creates user sessions from the specified cache value.
	 * @param id the identifier of the user
	 * @param value the cache value
	 * @return user sessions
	 */
	UserSessions<D, S> createUserSessions(String id, V value);
}
