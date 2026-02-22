/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionActivationListener;
import jakarta.servlet.http.HttpSessionEvent;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.server.util.Reference;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.container.ContainerProvider;

/**
 * Jakarta Servlet 6.1 container provider.
 * @author Paul Ferraro
 * @param <C> the session context type
 */
@MetaInfServices(ContainerProvider.class)
public class ServletContainerProvider<C> implements ContainerProvider.SessionAttributeEventListener<ServletContext, HttpSession, HttpSessionActivationListener, C> {
	/**
	 * Creates a new container provider.
	 */
	public ServletContainerProvider() {
	}

	@Override
	public String getId(ServletContext context) {
		return context.getVirtualServerName() + context.getContextPath();
	}

	@Override
	public HttpSession getSession(SessionManager<C> manager, ImmutableSession session, ServletContext context) {
		return new ImmutableHttpSession(manager, session, context);
	}

	@Override
	public HttpSession getSession(SessionManager<C> manager, Session<C> session, ServletContext context) {
		return new MutableHttpSession<>(manager, session, context);
	}

	@Override
	public HttpSession getSession(Reference<Session<C>> reference, String id, ServletContext context) {
		return new ReferencedHttpSession<>(reference, id, context);
	}

	@Override
	public Class<HttpSessionActivationListener> getSessionEventListenerClass() {
		return HttpSessionActivationListener.class;
	}

	@Override
	public Consumer<HttpSession> getPrePassivateEventNotifier(HttpSessionActivationListener listener) {
		return Consumer.of(HttpSessionEvent::new, listener::sessionWillPassivate);
	}

	@Override
	public Consumer<HttpSession> getPostActivateEventNotifier(HttpSessionActivationListener listener) {
		return Consumer.of(HttpSessionEvent::new, listener::sessionDidActivate);
	}

	@Override
	public String toString() {
		return "Jakarta Servlet 6.1";
	}
}
