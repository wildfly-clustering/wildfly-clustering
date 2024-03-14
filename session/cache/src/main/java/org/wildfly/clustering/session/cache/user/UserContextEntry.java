/*
- * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache.user;

import org.wildfly.clustering.server.util.Supplied;

/**
 * Cache entry that stores persistent and transient user context.
 * @author Paul Ferraro
 * @param <C> the persistent user context type
 * @param <T> the transient user context type
 */
public class UserContextEntry<C, T> implements UserContext<C, T> {

	private final C persistentContext;
	private final Supplied<T> transientContext = Supplied.cached();

	public UserContextEntry(C persistentContext) {
		this.persistentContext = persistentContext;
	}

	@Override
	public C getPersistentContext() {
		return this.persistentContext;
	}

	@Override
	public Supplied<T> getTransientContext() {
		return this.transientContext;
	}
}
