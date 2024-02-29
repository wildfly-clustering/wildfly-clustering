/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import org.wildfly.clustering.server.context.ContextualRegistration;
import org.wildfly.clustering.session.Session;

/**
 * A session facade whose lifecycle is managed as a {@link org.wildfly.clustering.server.context.Contextual}.
 * @author Paul Ferraro
 */
public class ContextualSessionRegistration<C> extends DecoratedSession<C> implements ContextualSession<C> {

	private final ContextualRegistration registration;

	public ContextualSessionRegistration(Session<C> session, Runnable closeTask) {
		super(session);
		this.registration = new ContextualRegistration(session, closeTask);
	}

	@Override
	public void end() {
		this.registration.end();
	}

	@Override
	public void close() {
		this.registration.close();
	}
}
