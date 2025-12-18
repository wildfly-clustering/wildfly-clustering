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
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.container.ContainerProvider;

/**
 * Jakarta Servlet 5.0 container provider.
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
	public HttpSession getDetachableSession(SessionManager<C> manager, ImmutableSession session, ServletContext context) {
		HttpSession detached = this.getDetachedSession(manager, session.getId(), context);
		return (detached != null) ? new DetachableHttpSession(new ImmutableHttpSession(session, context), detached) : null;
	}

	@Override
	public HttpSession getDetachedSession(SessionManager<C> manager, String id, ServletContext context) {
		Session<C> detached = manager.getDetachedSession(id);
		return (detached != null) ? new MutableHttpSession(detached, context) : null;
	}

	@Override
	public Class<HttpSessionActivationListener> getSessionEventListenerClass() {
		return HttpSessionActivationListener.class;
	}

	@Override
	public Consumer<HttpSession> getPrePassivateEventNotifier(HttpSessionActivationListener listener) {
		Consumer<HttpSessionEvent> eventNotifier = listener::sessionWillPassivate;
		return eventNotifier.compose(HttpSessionEvent::new);
	}

	@Override
	public Consumer<HttpSession> getPostActivateEventNotifier(HttpSessionActivationListener listener) {
		Consumer<HttpSessionEvent> eventNotifier = listener::sessionDidActivate;
		return eventNotifier.compose(HttpSessionEvent::new);
	}

	@Override
	public String toString() {
		return "Jakarta Servlet 5.0";
	}
}
