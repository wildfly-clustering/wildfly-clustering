/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;

/**
 * {@link Session} decorator that auto-detaches on {@link #close()}.
 * @author Paul Ferraro
 */
public class ManagedSession<C> extends DecoratedSession<C> {
	private final AtomicReference<Session<C>> session;
	private final Session<C> detachedSession;

	public <B extends Batch> ManagedSession(SessionManager<C, B> manager, Session<C> session, Consumer<ImmutableSession> closeTask) {
		this(new AttachedSession<>(session, closeTask), new DetachedSession<>(manager, session.getId(), session.getContext()));
	}

	ManagedSession(Session<C> attachedSession, Session<C> detachedSession) {
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
