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
 * Provider of a specification facade for a session.
 * @author Paul Ferraro
 * @param <C> the session manager context
 */
public class HttpSessionProvider<C> implements SessionSpecificationProvider<HttpSession, ServletContext> {

	private final SessionManager<C> manager;

	/**
	 * Creates a specification facade provider.
	 * @param manager the session manager.
	 */
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
