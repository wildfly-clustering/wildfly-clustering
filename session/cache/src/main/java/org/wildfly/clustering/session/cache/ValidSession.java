/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.function.Consumer;

import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionAttributes;
import org.wildfly.clustering.session.SessionMetaData;

/**
 * {@link Session} decorator whose methods throw an {@link IllegalStateException} if the session is not valid.
 * @author Paul Ferraro
 */
public class ValidSession<C> implements Session<C> {
	private final Session<C> session;
	private final Consumer<ImmutableSession> closeTask;

	public ValidSession(Session<C> session, Consumer<ImmutableSession> closeTask) {
		this.session = session;
		this.closeTask = closeTask;
	}

	private void validate() {
		if (!this.session.isValid()) {
			throw new IllegalStateException(this.session.getId());
		}
	}

	@Override
	public String getId() {
		return this.session.getId();
	}

	@Override
	public boolean isValid() {
		return this.session.isValid();
	}

	@Override
	public C getContext() {
		return this.session.getContext();
	}

	@Override
	public SessionMetaData getMetaData() {
		this.validate();
		return this.session.getMetaData();
	}

	@Override
	public SessionAttributes getAttributes() {
		this.validate();
		return this.session.getAttributes();
	}

	@Override
	public void invalidate() {
		this.validate();
		this.session.invalidate();
	}

	@Override
	public void close() {
		try {
			this.session.close();
		} finally {
			this.closeTask.accept(this.session);
		}
	}
}
