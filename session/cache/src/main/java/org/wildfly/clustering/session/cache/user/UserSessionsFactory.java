/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.cache.CacheEntryCreator;
import org.wildfly.clustering.cache.CacheEntryLocator;
import org.wildfly.clustering.cache.CacheEntryRemover;
import org.wildfly.clustering.session.user.UserSessions;

public interface UserSessionsFactory<V, D, S> extends CacheEntryCreator<String, V, Void>, CacheEntryLocator<String, V>, CacheEntryRemover<String> {

	UserSessions<D, S> createUserSessions(String id, V value);

	default CompletionStage<UserSessions<D, S>> createUserSessions(String id, CompletionStage<V> value) {
		return value.thenApply(v -> this.createUserSessions(id, v));
	}
}
