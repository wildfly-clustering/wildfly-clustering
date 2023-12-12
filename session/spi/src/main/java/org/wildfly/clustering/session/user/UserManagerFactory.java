/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.user;

import org.wildfly.clustering.cache.batch.Batch;

/**
 * Factory for creating SSO manager instances.
 * @param <C> the user context type
 * @param <D> deployment type
 * @param <S> session type
 * @param <B> batch type
 */
public interface UserManagerFactory<C, D, S, B extends Batch> {
	/**
	 * Creates a new user manager using the specified configuration.
	 * @param <L> local context type
	 * @param config a user manager configuration
	 * @return a new user manager
	 */
	<L> UserManager<C, L, D, S, B> createUserManager(UserManagerConfiguration<L, B> config);
}
