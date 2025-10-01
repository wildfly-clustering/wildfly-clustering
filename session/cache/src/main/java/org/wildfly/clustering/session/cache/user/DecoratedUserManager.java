/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import org.wildfly.clustering.server.manager.DecoratedManager;
import org.wildfly.clustering.server.service.Service;
import org.wildfly.clustering.session.user.User;
import org.wildfly.clustering.session.user.UserManager;

/**
 * A decorated user manager.
 * @author Paul Ferraro
 * @param <C> the persistent context type
 * @param <T> the transient context type
 * @param <D> the deployment type
 * @param <S> the session type
 */
public class DecoratedUserManager<C, T, D, S> extends DecoratedManager<String> implements UserManager<C, T, D, S> {

	private final UserManager<C, T, D, S> manager;

	/**
	 * Creates a user manager decorator.
	 * @param manager the decorated user manager
	 */
	public DecoratedUserManager(UserManager<C, T, D, S> manager) {
		this(manager, manager);
	}

	/**
	 * Creates a user manager decorator.
	 * @param manager the decorated user manager
	 * @param service an alternate service
	 */
	protected DecoratedUserManager(UserManager<C, T, D, S> manager, Service service) {
		super(manager, service);
		this.manager = manager;
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
