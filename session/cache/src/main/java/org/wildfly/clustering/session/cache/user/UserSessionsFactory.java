/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.cache.Creator;
import org.wildfly.clustering.cache.Locator;
import org.wildfly.clustering.cache.Remover;
import org.wildfly.clustering.session.user.UserSessions;

public interface UserSessionsFactory<V, D, S> extends Creator<String, V, Void>, Locator<String, V>, Remover<String> {

	UserSessions<D, S> createUserSessions(String id, V value);

	default CompletionStage<UserSessions<D, S>> createUserSessions(String id, CompletionStage<V> value) {
		return value.thenApply(v -> this.createUserSessions(id, v));
	}
}
