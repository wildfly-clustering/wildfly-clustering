/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.function.Consumer;

import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.Session;

/**
 * A {@link Session} decorator whose methods throw an {@link IllegalStateException} if the session is not valid.
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public class AttachedSession<C> extends DecoratedSession<C> {
	private final Runnable closeTask;

	AttachedSession(Session<C> session, Consumer<ImmutableSession> closeTask) {
		super(session);
		this.closeTask = Supplier.of(session).thenAccept(closeTask);
	}

	@Override
	public void close() {
		try {
			super.close();
		} finally {
			this.closeTask.run();
		}
	}
}
