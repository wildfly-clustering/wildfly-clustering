/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import java.util.function.Supplier;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

/**
 * Abstract {@link HttpSession} facade implementing basic methods.
 * @author Paul Ferraro
 */
public abstract class AbstractHttpSession implements HttpSession {
	private final Supplier<String> identifier;
	private final ServletContext context;

	AbstractHttpSession(Supplier<String> identifier, ServletContext context) {
		this.identifier = identifier;
		this.context = context;
	}

	@Override
	public String getId() {
		return this.identifier.get();
	}

	@Override
	public ServletContext getServletContext() {
		return this.context;
	}

	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return (object instanceof HttpSession session) && this.getId().equals(session.getId());
	}

	@Override
	public String toString() {
		return this.getId();
	}
}
