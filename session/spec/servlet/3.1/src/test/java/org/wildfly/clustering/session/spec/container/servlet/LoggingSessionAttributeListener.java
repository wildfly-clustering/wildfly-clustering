/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.spec.container.servlet;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

/**
 * Detects support (or lack thereof) for HttpSessionAttributeListener notifications in Spring Session.
 * @author Paul Ferraro
 */
@WebListener
public class LoggingSessionAttributeListener implements HttpSessionAttributeListener {

	@Override
	public void attributeAdded(HttpSessionBindingEvent event) {
		event.getSession().getServletContext().log("Session attribute added: " + event.getName());
	}

	@Override
	public void attributeRemoved(HttpSessionBindingEvent event) {
		event.getSession().getServletContext().log("Session attribute removed: " + event.getName());
	}

	@Override
	public void attributeReplaced(HttpSessionBindingEvent event) {
		event.getSession().getServletContext().log("Session attribute replaced: " + event.getName());
	}
}
