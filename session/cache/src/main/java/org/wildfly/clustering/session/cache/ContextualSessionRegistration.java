/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import org.wildfly.clustering.server.context.ContextualRegistration;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionAttributes;
import org.wildfly.clustering.session.SessionMetaData;

/**
 * A session facade whose lifecycle is managed as a {@link Completable}.
 * @author Paul Ferraro
 */
public class ContextualSessionRegistration<C> extends ContextualRegistration implements ContextualSession<C> {

	private final Session<C> session;

	public ContextualSessionRegistration(Session<C> session, Runnable closeTask) {
		super(session, closeTask);
		this.session = session;
	}

	@Override
	public String getId() {
		return this.session.getId();
	}

	@Override
	public C getContext() {
		return this.session.getContext();
	}

	@Override
	public SessionMetaData getMetaData() {
		return this.session.getMetaData();
	}

	@Override
	public SessionAttributes getAttributes() {
		return this.session.getAttributes();
	}

	@Override
	public boolean isValid() {
		return this.session.isValid();
	}

	@Override
	public void invalidate() {
		this.session.invalidate();
		this.close();
	}
}
