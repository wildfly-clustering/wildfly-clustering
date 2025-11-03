/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import java.util.Collections;
import java.util.Enumeration;

import jakarta.servlet.http.HttpSession;

/**
 * Abstract {@link HttpSession} facade implementing deprecated methods.
 * @author Paul Ferraro
 */
public abstract class AbstractHttpSession implements HttpSession {
	/**
	 * Creates a new session facade.
	 */
	protected AbstractHttpSession() {
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
	public jakarta.servlet.http.HttpSessionContext getSessionContext() {
		return new jakarta.servlet.http.HttpSessionContext() {
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
