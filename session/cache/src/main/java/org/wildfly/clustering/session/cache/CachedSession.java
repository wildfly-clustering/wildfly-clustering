/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import org.wildfly.clustering.session.Session;

/**
 * A session facade whose lifecycle is managed by a {@link org.wildfly.clustering.server.cache.Cache}.
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public class CachedSession<C> extends DecoratedSession<C> implements CacheableSession<C> {

	private final Runnable closeTask;

	public CachedSession(Session<C> session, Runnable closeTask) {
		super(session);
		this.closeTask = closeTask;
	}

	@Override
	public void close() {
		this.closeTask.run();
	}
}
