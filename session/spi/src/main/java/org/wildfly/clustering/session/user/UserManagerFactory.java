/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.user;

import org.wildfly.clustering.cache.batch.Batch;

/**
 * Factory for creating user manager instances.
 * @param <C> the user context type
 * @param <D> the deployment type
 * @param <S> the session type
 * @param <B> thge batch type
 */
public interface UserManagerFactory<C, D, S, B extends Batch> {
	/**
	 * Creates a new user manager using the specified configuration.
	 * @param <T> the transient user context type
	 * @param configuration a user manager configuration
	 * @return a new user manager
	 */
	<T> UserManager<C, T, D, S, B> createUserManager(UserManagerConfiguration<T, B> configuration);
}
