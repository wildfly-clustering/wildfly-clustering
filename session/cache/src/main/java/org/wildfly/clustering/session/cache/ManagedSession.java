/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.concurrent.atomic.AtomicReference;

import org.wildfly.clustering.session.Session;

/**
 * {@link Session} decorator that auto-detaches on {@link #close()}.
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public class ManagedSession<C> extends DecoratedSession<C> {
	private final AtomicReference<Session<C>> session;
	private final Session<C> detachedSession;

	/**
	 * Creates a managed session.
	 * @param attachedSession the attached session
	 * @param detachedSession the detached session
	 */
	public ManagedSession(Session<C> attachedSession, Session<C> detachedSession) {
		this(new AtomicReference<>(attachedSession), detachedSession);
	}

	private ManagedSession(AtomicReference<Session<C>> session, Session<C> detachedSession) {
		super(session::get);
		this.session = session;
		this.detachedSession = detachedSession;
	}

	@Override
	public void close() {
		this.session.getAndSet(this.detachedSession).close();
	}
}
