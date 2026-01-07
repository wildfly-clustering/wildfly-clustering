/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import java.util.Optional;
import java.util.function.BiFunction;

import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.server.cache.Cache;
import org.wildfly.clustering.server.cache.CacheFactory;
import org.wildfly.clustering.session.user.User;
import org.wildfly.clustering.session.user.UserManager;

/**
 * A user manager that shares user references between concurrent threads.
 * @author Paul Ferraro
 * @param <C> the persistent context type
 * @param <T> the transient context type
 * @param <D> the deployment type
 * @param <S> the session type
 */
public class CachedUserManager<C, T, D, S> extends DecoratedUserManager<C, T, D, S> {
	static final System.Logger LOGGER = System.getLogger(CachedUserManager.class.getName());

	private final Cache<String, CacheableUser<C, T, D, S>> cache;
	private final BiFunction<String, Runnable, CacheableUser<C, T, D, S>> findUser;

	/**
	 * Creates a cached user manager.
	 * @param manager the decorated user manager
	 * @param cacheFactory a cache factory
	 */
	public CachedUserManager(UserManager<C, T, D, S> manager, CacheFactory cacheFactory) {
		super(manager);
		this.findUser = (id, closeTask) -> Optional.ofNullable(manager.findUser(id)).map(user -> new CachedUser<>(user, closeTask)).orElse(null);
		this.cache = cacheFactory.createCache(Consumer.of(), Consumer.<User<C, T, D, S>>close().compose(CacheableUser::get));
	}

	@Override
	public User<C, T, D, S> createUser(String id, C context) {
		BiFunction<String, Runnable, CacheableUser<C, T, D, S>> createUser = (userId, closeTask) -> new CachedUser<>(super.createUser(userId, context), closeTask);
		return this.cache.computeIfAbsent(id, createUser);
	}

	@Override
	public User<C, T, D, S> findUser(String id) {
		return this.cache.computeIfAbsent(id, this.findUser);
	}
}
