/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.spec.servlet;

import java.util.function.Consumer;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionActivationListener;
import jakarta.servlet.http.HttpSessionEvent;

import org.wildfly.clustering.session.spec.SessionEventListenerSpecificationProvider;

/**
 * @author Paul Ferraro
 */
public enum HttpSessionActivationListenerProvider implements SessionEventListenerSpecificationProvider<HttpSession, HttpSessionActivationListener> {
	INSTANCE;

	@Override
	public Class<HttpSessionActivationListener> getEventListenerClass() {
		return HttpSessionActivationListener.class;
	}

	@Override
	public Consumer<HttpSession> preEvent(HttpSessionActivationListener listener) {
		return session -> listener.sessionWillPassivate(new HttpSessionEvent(session));
	}

	@Override
	public Consumer<HttpSession> postEvent(HttpSessionActivationListener listener) {
		return session -> listener.sessionDidActivate(new HttpSessionEvent(session));
	}

	@Override
	public HttpSessionActivationListener asEventListener(Consumer<HttpSession> preEvent, Consumer<HttpSession> postEvent) {
		return new HttpSessionActivationListener() {
			@Override
			public void sessionWillPassivate(HttpSessionEvent event) {
				preEvent.accept(event.getSession());
			}

			@Override
			public void sessionDidActivate(HttpSessionEvent event) {
				postEvent.accept(event.getSession());
			}
		};
	}
}
