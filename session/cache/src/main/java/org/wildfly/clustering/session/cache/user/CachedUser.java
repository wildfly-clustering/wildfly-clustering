/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.session.user.User;

/**
 * A cached user.
 * @author Paul Ferraro
 * @param <C> the persistent context type
 * @param <T> the transient context type
 * @param <D> the deployment type
 * @param <S> the session type
 */
public class CachedUser<C, T, D, S> extends DecoratedUser<C, T, D, S> implements CacheableUser<C, T, D, S> {

	private final Runnable closeTask;

	/**
	 * Creates a cached user.
	 * @param user the user object.
	 * @param closeTask a task to run on {@link User#close()}.
	 */
	public CachedUser(User<C, T, D, S> user, Runnable closeTask) {
		super(Supplier.of(user));
		this.closeTask = closeTask;
	}

	@Override
	public void close() {
		this.closeTask.run();
	}
}
