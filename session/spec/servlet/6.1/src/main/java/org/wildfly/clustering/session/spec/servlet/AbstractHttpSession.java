/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.spec.servlet;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import org.wildfly.clustering.session.ImmutableSession;

/**
 * Abstract {@link HttpSession} facade implementing read-only and deprecated methods.
 * @author Paul Ferraro
 */
public abstract class AbstractHttpSession implements HttpSession {

	private final ImmutableSession session;
	private final ServletContext context;

	/**
	 * Creates a specification facade for a session.
	 * @param session the decorated session
	 * @param context the associated servlet context
	 */
	protected AbstractHttpSession(ImmutableSession session, ServletContext context) {
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
		return (int) this.session.getMetaData().getTimeout().getSeconds();
	}

	@Override
	public long getLastAccessedTime() {
		return this.session.getMetaData().getLastAccessStartTime().toEpochMilli();
	}

	@Override
	public boolean isNew() {
		return this.session.getMetaData().isNew();
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
	public int hashCode() {
		return this.session.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof AbstractHttpSession session)) return false;
		return Objects.equals(this.session, session.session);
	}

	@Override
	public String toString() {
		return this.session.toString();
	}
}
