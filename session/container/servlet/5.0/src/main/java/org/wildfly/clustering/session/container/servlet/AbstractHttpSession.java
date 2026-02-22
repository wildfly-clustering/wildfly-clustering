/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import java.util.Collections;
import java.util.Enumeration;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionContext;

/**
 * Abstract {@link HttpSession} facade implementing basic methods.
 * @author Paul Ferraro
 */
public abstract class AbstractHttpSession implements HttpSession {

	private final ServletContext context;

	AbstractHttpSession(ServletContext context) {
		this.context = context;
	}

	@Override
	public ServletContext getServletContext() {
		return this.context;
	}

	@Deprecated
	@Override
	public HttpSessionContext getSessionContext() {
		return new HttpSessionContext() {
			@Override
			public HttpSession getSession(String sessionId) {
				return null;
			}

			@Override
			public Enumeration<String> getIds() {
				return Collections.emptyEnumeration();
			}
		};
	}

	@Deprecated
	@Override
	public Object getValue(String name) {
		return this.getAttribute(name);
	}

	@Deprecated
	@Override
	public String[] getValueNames() {
		return Collections.list(this.getAttributeNames()).toArray(String[]::new);
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

	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return (object instanceof HttpSession session) ? this.getId().equals(session.getId()) : false;
	}

	@Override
	public String toString() {
		return this.getId();
	}
}
