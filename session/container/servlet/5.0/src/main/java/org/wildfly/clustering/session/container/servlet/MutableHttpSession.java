/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import java.time.Duration;

import jakarta.servlet.ServletContext;

import org.wildfly.clustering.session.Session;

/**
 * A mutable {@link jakarta.servlet.http.HttpSession} facade.
 * @author Paul Ferraro
 */
public class MutableHttpSession extends ImmutableHttpSession {

	private final Session<?> session;

	MutableHttpSession(Session<?> session, ServletContext context) {
		super(session, context);
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
