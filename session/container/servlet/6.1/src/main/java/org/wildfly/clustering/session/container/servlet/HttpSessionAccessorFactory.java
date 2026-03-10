/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.session.SessionManager;

/**
 * An HttpSession accessor factory.
 * @author Paul Ferraro
 * @param <C> the session context type
 */
public class HttpSessionAccessorFactory<C> implements Function<String, HttpSession.Accessor> {

	private final SessionManager<C> manager;
	private final ServletContext context;

	/**
	 * Constructs a session accessor factory.
	 * @param manager a session manager
	 * @param context a servlet context
	 */
	public HttpSessionAccessorFactory(SessionManager<C> manager, ServletContext context) {
		this.manager = manager;
		this.context = context;
	}

	@Override
	public HttpSession.Accessor apply(String id) {
		return new HttpSessionAccessor<>(this.manager.getSessionReference(id), this.context);
	}
}
