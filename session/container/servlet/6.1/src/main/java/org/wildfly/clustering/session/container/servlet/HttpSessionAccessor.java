/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.server.util.Reference;
import org.wildfly.clustering.session.Session;

/**
 * A session accessor that reads from a session reference.
 * @author Paul Ferraro
 * @param <C> the session context type
 */
public class HttpSessionAccessor<C> implements HttpSession.Accessor {

	private final Reference<Session<C>> reference;
	private final ServletContext context;

	/**
	 * Constructs an {@link HttpSession} accessor.
	 * @param reference a session reference
	 * @param context the servlet context of the session
	 */
	public HttpSessionAccessor(Reference<Session<C>> reference, ServletContext context) {
		this.reference = reference;
		this.context = context;
	}

	@Override
	public void access(java.util.function.Consumer<HttpSession> consumer) {
		this.reference.getReader().consume(Consumer.of(this::wrap, consumer));
	}

	private HttpSession wrap(Session<C> session) {
		if (session == null) {
			throw new IllegalStateException();
		}
		return new MutableHttpSession<>(this, session, this.context);
	}
}
