/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.spec.servlet;

import java.util.function.Consumer;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.spec.SessionSpecificationProvider;

/**
 * Provides an HttpSession for use with application callbacks.
 * @author Paul Ferraro
 * @param <C> session context type
 */
public class HttpSessionProvider<C> implements SessionSpecificationProvider<HttpSession, ServletContext> {

	private final SessionManager<C> manager;

	public HttpSessionProvider(SessionManager<C> manager) {
		this.manager = manager;
	}

	@Override
	public HttpSession asSession(ImmutableSession session, ServletContext context) {
		HttpSession detached = new DetachedHttpSession<>(this.manager, session.getId(), context);
		HttpSession.Accessor accessor = new HttpSession.Accessor() {
			@Override
			public void access(Consumer<HttpSession> consumer) {
				consumer.accept(detached);
			}
		};
		return new AbstractHttpSession(session, context) {
			@Override
			public void setMaxInactiveInterval(int interval) {
				// Do nothing
			}

			@Override
			public void setAttribute(String name, Object value) {
				// Do nothing
			}

			@Override
			public void removeAttribute(String name) {
				// Do nothing
			}

			@Override
			public void invalidate() {
				// Do nothing
			}

			@Override
			public Accessor getAccessor() {
				return accessor;
			}
		};
	}
}
