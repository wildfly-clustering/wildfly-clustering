/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import java.time.Duration;

import jakarta.servlet.ServletContext;

import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;

/**
 * A mutable {@link jakarta.servlet.http.HttpSession} facade.
 * @author Paul Ferraro
 * @param <C> the session context type
 */
public class MutableHttpSession<C> extends ImmutableHttpSession {

	private final Session<C> session;

	/**
	 * Creates an immutable {@link jakarta.servlet.http.HttpSession}.
	 * @param manager the manager of the specified session
	 * @param session the decorated session
	 * @param context the associated servlet context
	 */
	public MutableHttpSession(SessionManager<C> manager, Session<C> session, ServletContext context) {
		super(manager, session, context);
		this.session = session;
	}

	MutableHttpSession(Accessor accessor, Session<C> session, ServletContext context) {
		super(Supplier.of(accessor), session, context);
		this.session = session;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		this.session.getMetaData().setMaxIdle(Duration.ofSeconds(interval));
	}

	@Override
	public void setAttribute(String name, Object value) {
		this.session.getAttributes().put(name, value);
	}

	@Override
	public void removeAttribute(String name) {
		this.session.getAttributes().remove(name);
	}

	@Override
	public void invalidate() {
		this.session.invalidate();
	}
}
