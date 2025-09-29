/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.Map;
import java.util.function.Consumer;

import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionMetaData;

/**
 * Decorated {@link Session} whose methods throw an {@link IllegalStateException} if the session is not valid.
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public class AttachedSession<C> extends DecoratedSession<C> {
	private final Consumer<ImmutableSession> closeTask;

	AttachedSession(Session<C> session, Consumer<ImmutableSession> closeTask) {
		super(Supplier.of(session));
		this.closeTask = closeTask;
	}

	private void validate() {
		if (!this.isValid()) {
			throw new IllegalStateException(this.getId());
		}
	}

	@Override
	public SessionMetaData getMetaData() {
		this.validate();
		return super.getMetaData();
	}

	@Override
	public Map<String, Object> getAttributes() {
		this.validate();
		return super.getAttributes();
	}

	@Override
	public void invalidate() {
		this.validate();
		super.invalidate();
	}

	@Override
	public void close() {
		try {
			super.close();
		} finally {
			this.closeTask.accept(this.get());
		}
	}
}
