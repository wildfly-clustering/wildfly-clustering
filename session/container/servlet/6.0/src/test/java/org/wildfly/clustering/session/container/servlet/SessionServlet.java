/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import java.io.IOException;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.IntFunction;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.wildfly.clustering.session.container.SessionManagementEndpointConfiguration;

/**
 * @author Paul Ferraro
 */
public class SessionServlet<T> extends HttpServlet {
	private static final long serialVersionUID = 2864700300414161976L;

	private final IntFunction<T> factory;
	private final BiFunction<HttpSession, String, OptionalInt> accessor;
	private final BiFunction<HttpSession, String, OptionalInt> incrementor;

	protected SessionServlet(IntFunction<T> factory, BiFunction<HttpSession, String, OptionalInt> accessor, BiFunction<HttpSession, String, OptionalInt> incrementor) {
		this.factory = factory;
		this.accessor = accessor;
		this.incrementor = incrementor;
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String query = request.getQueryString();
		this.getServletContext().log(String.format("[%s] %s%s", request.getMethod(), request.getRequestURI(), (query != null) ? '?' + query : ""));
		super.service(request, response);

		HttpSession session = request.getSession(false);
		if (session != null) {
			response.setHeader(SessionManagementEndpointConfiguration.SESSION_ID, session.getId());
		}
	}

	@Override
	public void doHead(HttpServletRequest request, HttpServletResponse response) {
		recordSession(request, response, this.accessor);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		recordSession(request, response, this.incrementor);
	}

	private static void recordSession(HttpServletRequest request, HttpServletResponse response, BiFunction<HttpSession, String, OptionalInt> function) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			function.apply(session, SessionManagementEndpointConfiguration.COUNTER).ifPresent(value -> response.setIntHeader(SessionManagementEndpointConfiguration.COUNTER, value));
			UUID value = (UUID) session.getAttribute(SessionManagementEndpointConfiguration.IMMUTABLE);
			if (value != null) {
				response.setHeader(SessionManagementEndpointConfiguration.IMMUTABLE, value.toString());
			}
		}
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(true);

		UUID immutableValue = UUID.randomUUID();
		session.setAttribute(SessionManagementEndpointConfiguration.IMMUTABLE, immutableValue);
		response.addHeader(SessionManagementEndpointConfiguration.IMMUTABLE, immutableValue.toString());

		int count = 0;
		session.setAttribute(SessionManagementEndpointConfiguration.COUNTER, this.factory.apply(count));
		response.addIntHeader(SessionManagementEndpointConfiguration.COUNTER, count);
	}

	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}
}
