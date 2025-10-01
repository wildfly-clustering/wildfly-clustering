/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import java.util.function.Supplier;

import org.wildfly.clustering.session.user.User;
import org.wildfly.clustering.session.user.UserSessions;

/**
 * A user decorator.
 * @author Paul Ferraro
 * @param <C> the persistent context type
 * @param <T> the transient context type
 * @param <D> the deployment type
 * @param <S> the session type
 */
public class DecoratedUser<C, T, D, S> implements User<C, T, D, S>, Supplier<User<C, T, D, S>> {

	private final Supplier<User<C, T, D, S>> reference;

	/**
	 * Creates a user decorator.
	 * @param reference a reference to a user
	 */
	public DecoratedUser(Supplier<User<C, T, D, S>> reference) {
		this.reference = reference;
	}

	@Override
	public User<C, T, D, S> get() {
		return this.reference.get();
	}

	@Override
	public String getId() {
		return this.reference.get().getId();
	}

	@Override
	public C getPersistentContext() {
		return this.reference.get().getPersistentContext();
	}

	@Override
	public T getTransientContext() {
		return this.reference.get().getTransientContext();
	}

	@Override
	public UserSessions<D, S> getSessions() {
		return this.reference.get().getSessions();
	}

	@Override
	public boolean isValid() {
		return this.reference.get().isValid();
	}

	@Override
	public void invalidate() {
		this.reference.get().invalidate();
	}

	@Override
	public void close() {
		this.reference.get().close();
	}

	@Override
	public int hashCode() {
		return this.reference.get().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return this.reference.get().equals(object);
	}

	@Override
	public String toString() {
		return this.reference.get().toString();
	}
}
