/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache.user;

import java.util.Map;
import java.util.function.Supplier;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.server.manager.IdentifierFactory;
import org.wildfly.clustering.session.user.User;
import org.wildfly.clustering.session.user.UserManager;

/**
 * A default user manager implementation that delegates to a user factory.
 * @param <CV> the user context value type
 * @param <C> the persistent context type
 * @param <T> the transient context type
 * @param <SV> the user sessions value type
 * @param <D> the deployment type
 * @param <S> the session type
 */
public class DefaultUserManager<CV, C, T, SV, D, S> implements UserManager<C, T, D, S> {

	private final UserFactory<CV, C, T, SV, D, S> factory;
	private final Supplier<Batch> batchFactory;
	private final IdentifierFactory<String> identifierFactory;

	public DefaultUserManager(UserFactory<CV, C, T, SV, D, S> factory, IdentifierFactory<String> identifierFactory, Supplier<Batch> batchFactory) {
		this.factory = factory;
		this.batchFactory = batchFactory;
		this.identifierFactory = identifierFactory;
	}

	@Override
	public User<C, T, D, S> createUser(String id, C context) {
		Map.Entry<CV, SV> value = this.factory.createValue(id, context);
		return this.factory.createUser(id, value);
	}

	@Override
	public User<C, T, D, S> findUser(String id) {
		Map.Entry<CV, SV> value = this.factory.findValue(id);
		return (value != null) ? this.factory.createUser(id, value) : null;
	}

	@Override
	public Supplier<Batch> getBatchFactory() {
		return this.batchFactory;
	}

	@Override
	public Supplier<String> getIdentifierFactory() {
		return this.identifierFactory;
	}

	@Override
	public void start() {
		this.identifierFactory.start();
	}

	@Override
	public void stop() {
		this.identifierFactory.stop();
	}
}
