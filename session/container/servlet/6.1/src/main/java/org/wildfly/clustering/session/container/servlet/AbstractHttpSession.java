/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

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
