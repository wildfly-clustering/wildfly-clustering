/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.spec.servlet;

import java.util.Collections;
import java.util.Enumeration;
import java.util.function.Consumer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;

import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.spec.SessionSpecificationProvider;

/**
 * @author Paul Ferraro
 */
public enum JavaServletSpecificationProvider implements SessionSpecificationProvider<HttpSession, ServletContext, HttpSessionActivationListener> {
	INSTANCE;

	@Override
	public HttpSession asSession(ImmutableSession session, ServletContext context) {
		return new AbstractImmutableHttpSession() {
			@Override
			public ServletContext getServletContext() {
				return context;
			}

			@Override
			public String getId() {
				return session.getId();
			}

			@Override
			public long getCreationTime() {
				return session.getMetaData().getCreationTime().toEpochMilli();
			}

			@Override
			public int getMaxInactiveInterval() {
				return (int) session.getMetaData().getTimeout().getSeconds();
			}

			@Override
			public long getLastAccessedTime() {
				return session.getMetaData().getLastAccessTime().toEpochMilli();
			}

			@Override
			public boolean isNew() {
				return session.getMetaData().isNew();
			}

			@Override
			public Enumeration<String> getAttributeNames() {
				return Collections.enumeration(session.getAttributes().getAttributeNames());
			}

			@Override
			public Object getAttribute(String name) {
				return session.getAttributes().getAttribute(name);
			}
		};
	}

	@Override
	public Class<HttpSessionActivationListener> getSessionActivationListenerClass() {
		return HttpSessionActivationListener.class;
	}

	@Override
	public Consumer<HttpSession> prePassivate(HttpSessionActivationListener listener) {
		return session -> listener.sessionWillPassivate(new HttpSessionEvent(session));
	}

	@Override
	public Consumer<HttpSession> postActivate(HttpSessionActivationListener listener) {
		return session -> listener.sessionDidActivate(new HttpSessionEvent(session));
	}

	@Override
	public HttpSessionActivationListener asSessionActivationListener(Consumer<HttpSession> prePassivate, Consumer<HttpSession> postActivate) {
		return new HttpSessionActivationListener() {
			@Override
			public void sessionWillPassivate(HttpSessionEvent event) {
				prePassivate.accept(event.getSession());
			}

			@Override
			public void sessionDidActivate(HttpSessionEvent event) {
				postActivate.accept(event.getSession());
			}
		};
	}
}
