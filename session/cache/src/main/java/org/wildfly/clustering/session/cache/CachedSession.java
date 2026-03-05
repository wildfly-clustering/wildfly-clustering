/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.cache.batch.SuspendedBatch;
import org.wildfly.clustering.session.Session;

/**
 * A session facade whose lifecycle is managed by a {@link org.wildfly.clustering.server.cache.Cache}.
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public class CachedSession<C> extends DecoratedSession<C> implements CacheableSession<C> {
	private final Session<C> session;
	private final SuspendedBatch batch;
	private final Runnable closeTask;

	/**
	 * Creates a cached session decorator
	 * @param session a session
	 * @param batch the batch associated with this session
	 * @param closeTask a task to run on {@link Session#close()}.
	 */
	public CachedSession(Session<C> session, SuspendedBatch batch, Runnable closeTask) {
		super(session);
		this.session = session;
		this.batch = batch;
		this.closeTask = closeTask;
	}

	@Override
	public Session<C> get() {
		return this.session;
	}

	@Override
	public void close() {
		this.closeTask.run();
	}

	@Override
	public Batch resume() {
		return this.batch.resume();
	}
}
