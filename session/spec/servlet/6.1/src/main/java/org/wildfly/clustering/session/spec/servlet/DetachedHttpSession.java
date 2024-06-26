/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.spec.servlet;

import java.time.Duration;
import java.util.function.Consumer;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;

/**
 * A detached HttpSession for use by {@link jakarta.servlet.http.HttpSession.Accessor}.
 * @author Paul Ferraro
 * @param <C> session context type
 */
public class DetachedHttpSession<C> extends AbstractHttpSession implements HttpSession.Accessor {

	private final Session<C> session;

	public DetachedHttpSession(SessionManager<C> manager, String id, ServletContext context) {
		this(manager.getDetachedSession(id), context);
	}

	private DetachedHttpSession(Session<C> session, ServletContext context) {
		super(session, context);
		this.session = session;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		this.session.getMetaData().setTimeout(Duration.ofSeconds(interval));
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

	@Override
	public void access(Consumer<HttpSession> consumer) {
		consumer.accept(this);
	}
}
