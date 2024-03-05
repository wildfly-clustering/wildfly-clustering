/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.spec.servlet;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.wildfly.clustering.session.ImmutableSession;

/**
 * @author Paul Ferraro
 */
public abstract class AbstractHttpSession implements HttpSession {

	private final ImmutableSession session;
	private final ServletContext context;

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
		return this.session.getMetaData().getLastAccessTime().toEpochMilli();
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

	@Deprecated
	@Override
	public String[] getValueNames() {
		return Collections.list(this.getAttributeNames()).toArray(new String[0]);
	}

	@Deprecated
	@Override
	public Object getValue(String name) {
		return this.getAttribute(name);
	}

	@Deprecated
	@Override
	public void putValue(String name, Object value) {
		this.setAttribute(name, value);
	}

	@Deprecated
	@Override
	public void removeValue(String name) {
		this.removeAttribute(name);
	}

	@Deprecated
	@Override
	public javax.servlet.http.HttpSessionContext getSessionContext() {
		return new javax.servlet.http.HttpSessionContext() {
			@Override
			public Enumeration<String> getIds() {
				return Collections.enumeration(Collections.<String>emptyList());
			}

			@Override
			public HttpSession getSession(String sessionId) {
				return null;
			}
		};
	}

	@Override
	public int hashCode() {
		return this.session.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof AbstractHttpSession)) return false;
		AbstractHttpSession session = (AbstractHttpSession) object;
		return Objects.equals(this.session, session.session);
	}

	@Override
	public String toString() {
		return this.session.toString();
	}
}
