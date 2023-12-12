/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache.user;

import java.util.Map;
import java.util.function.Supplier;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.batch.Batcher;
import org.wildfly.clustering.server.manager.IdentifierFactory;
import org.wildfly.clustering.session.user.User;
import org.wildfly.clustering.session.user.UserManager;

public class DefaultUserManager<CV, C, L, SV, D, S, B extends Batch> implements UserManager<C, L, D, S, B> {

	private final UserFactory<CV, C, L, SV, D, S> factory;
	private final Batcher<B> batcher;
	private final IdentifierFactory<String> identifierFactory;

	public DefaultUserManager(UserFactory<CV, C, L, SV, D, S> factory, IdentifierFactory<String> identifierFactory, Batcher<B> batcher) {
		this.factory = factory;
		this.batcher = batcher;
		this.identifierFactory = identifierFactory;
	}

	@Override
	public User<C, L, D, S> createUser(String id, C context) {
		Map.Entry<CV, SV> value = this.factory.createValue(id, context);
		return this.factory.createUser(id, value);
	}

	@Override
	public User<C, L, D, S> findUser(String id) {
		Map.Entry<CV, SV> value = this.factory.findValue(id);
		return (value != null) ? this.factory.createUser(id, value) : null;
	}

	@Override
	public Batcher<B> getBatcher() {
		return this.batcher;
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
