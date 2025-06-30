/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.session.user.User;
import org.wildfly.clustering.session.user.UserManager;

/**
 * @author Paul Ferraro
 * @param <C> the persistent context type
 * @param <T> the transient context type
 * @param <D> the deployment type
 * @param <S> the session type
 */
public class DecoratedUserManager<C, T, D, S> implements UserManager<C, T, D, S> {

	private final UserManager<C, T, D, S> manager;

	public DecoratedUserManager(UserManager<C, T, D, S> manager) {
		this.manager = manager;
	}

	@Override
	public Supplier<Batch> getBatchFactory() {
		return this.manager.getBatchFactory();
	}

	@Override
	public Supplier<String> getIdentifierFactory() {
		return this.manager.getIdentifierFactory();
	}

	@Override
	public boolean isStarted() {
		return this.manager.isStarted();
	}

	@Override
	public void start() {
		this.manager.start();
	}

	@Override
	public void stop() {
		this.manager.stop();
	}

	@Override
	public User<C, T, D, S> createUser(String id, C context) {
		return this.manager.createUser(id, context);
	}

	@Override
	public User<C, T, D, S> findUser(String id) {
		return this.manager.findUser(id);
	}
}
