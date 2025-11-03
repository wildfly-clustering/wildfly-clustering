/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Supplier;

/**
 * A detachable {@link javax.servlet.http.HttpSession} facade.
 * @author Paul Ferraro
 */
public class DetachableHttpSession extends AbstractHttpSession {

	private final Supplier<HttpSession> reader;
	private final Supplier<HttpSession> writer;

	DetachableHttpSession(HttpSession attachedSession, HttpSession detachedSession) {
		AtomicReference<HttpSession> reference = new AtomicReference<>();
		this.reader = reference::get;
		Supplier<HttpSession> attached = Supplier.of(attachedSession);
		// Auto-detach if writing to session
		this.writer = attached.thenApply(Function.of(reference::set, attached));
	}

	@Override
	public String getId() {
		return this.reader.get().getId();
	}

	@Override
	public ServletContext getServletContext() {
		return this.reader.get().getServletContext();
	}

	@Override
	public boolean isNew() {
		return this.reader.get().isNew();
	}

	@Override
	public long getCreationTime() {
		return this.reader.get().getCreationTime();
	}

	@Override
	public long getLastAccessedTime() {
		return this.reader.get().getLastAccessedTime();
	}

	@Override
	public int getMaxInactiveInterval() {
		return this.reader.get().getMaxInactiveInterval();
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		this.writer.get().setMaxInactiveInterval(interval);
	}

	@Override
	public Object getAttribute(String name) {
		return this.reader.get().getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return this.reader.get().getAttributeNames();
	}

	@Override
	public void setAttribute(String name, Object value) {
		this.writer.get().setAttribute(name, value);
	}

	@Override
	public void removeAttribute(String name) {
		this.writer.get().removeAttribute(name);
	}

	@Override
	public void invalidate() {
		this.writer.get().invalidate();
	}
}
