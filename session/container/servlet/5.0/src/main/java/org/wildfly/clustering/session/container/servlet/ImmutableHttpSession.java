/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import java.time.Duration;
import java.util.Collections;
import java.util.Enumeration;

import jakarta.servlet.ServletContext;

import org.wildfly.clustering.session.ImmutableSession;

/**
 * An immutable {@link jakarta.servlet.http.HttpSession} facade.
 * @author Paul Ferraro
 */
public class ImmutableHttpSession extends AbstractHttpSession {

	private final ImmutableSession session;
	private final ServletContext context;

	/**
	 * Creates a specification facade for a session.
	 * @param session the decorated session
	 * @param context the associated servlet context
	 */
	ImmutableHttpSession(ImmutableSession session, ServletContext context) {
		this.session = session;
		this.context = context;
	}

	@Override
	public ServletContext getServletContext() {
		return this.context;
	}

	@Override
	public String getId() {
		return this.session.getId();
	}

	@Override
	public long getCreationTime() {
		return this.session.getMetaData().getCreationTime().toEpochMilli();
	}

	@Override
	public int getMaxInactiveInterval() {
		// Per Servlet specification, 0 or less indicates no timeout
		return (int) this.session.getMetaData().getMaxIdle().orElse(Duration.ZERO).getSeconds();
	}

	@Override
	public long getLastAccessedTime() {
		// Specification does not clearly define what this method should return for new sessions
		// Per Tomcat, default to creation time for new session
		return this.session.getMetaData().getLastAccessStartTime().orElse(this.session.getMetaData().getCreationTime()).toEpochMilli();
	}

	@Override
	public boolean isNew() {
		return this.session.getMetaData().getLastAccessTime().isEmpty();
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(this.session.getAttributes().keySet());
	}

	@Override
	public Object getAttribute(String name) {
		return this.session.getAttributes().get(name);
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
	}

	@Override
	public void setAttribute(String name, Object value) {
	}

	@Override
	public void removeAttribute(String name) {
	}

	@Override
	public void invalidate() {
	}
}
