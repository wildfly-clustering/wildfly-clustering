/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.spec.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.spec.SessionSpecificationProvider;

/**
 * @author Paul Ferraro
 */
public enum HttpSessionProvider implements SessionSpecificationProvider<HttpSession, ServletContext> {
	INSTANCE;

	@Override
	public HttpSession asSession(ImmutableSession session, ServletContext context) {
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
		};
	}
}
