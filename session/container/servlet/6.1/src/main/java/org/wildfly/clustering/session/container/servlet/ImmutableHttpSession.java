/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import java.time.Duration;
import java.util.Collections;
import java.util.Enumeration;

import jakarta.servlet.ServletContext;

import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.SessionManager;

/**
 * An immutable {@link jakarta.servlet.http.HttpSession} facade.
 * @author Paul Ferraro
 */
public class ImmutableHttpSession extends AbstractHttpSession {

	private final Supplier<Accessor> accessor;
	private final ImmutableSession session;

	/**
	 * Creates an immutable {@link jakarta.servlet.http.HttpSession}.
	 * @param <C> the session context type
	 * @param manager the manager of the specified session
	 * @param session the decorated session
	 * @param context the associated servlet context
	 */
	public <C> ImmutableHttpSession(SessionManager<C> manager, ImmutableSession session, ServletContext context) {
		this(new Supplier<>() {
			@Override
			public Accessor get() {
				return new HttpSessionAccessor<>(manager.getSessionReference(session.getId()), context);
			}
		}, session, context);
	}

	ImmutableHttpSession(Supplier<Accessor> accessor, ImmutableSession session, ServletContext context) {
		super(context);
		this.accessor = accessor;
		this.session = session;
	}

	@Override
	public String getId() {
		return this.session.getId();
	}

	@Override
	public Accessor getAccessor() {
		return this.accessor.get();
	}

	@Override
	public long getCreationTime() {
		return this.session.getMetaData().getCreationTime().toEpochMilli();
	}

	@Override
	public int getMaxInactiveInterval() {
		// Per Servlet specification, 0 or less indicates no timeout
		return (int) this.session.getMetaData().getMaxIdle().orElse(Duration.ZERO).getSeconds();
	}

	@Override
	public long getLastAccessedTime() {
		// Specification does not clearly define what this method should return for new sessions
		// Per Tomcat, default to creation time for new session
		return this.session.getMetaData().getLastAccessStartTime().orElse(this.session.getMetaData().getCreationTime()).toEpochMilli();
	}

	@Override
	public boolean isNew() {
		return this.session.getMetaData().getLastAccessTime().isEmpty();
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(this.session.getAttributes().keySet());
	}

	@Override
	public Object getAttribute(String name) {
		return this.session.getAttributes().get(name);
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		throw new IllegalStateException();
	}

	@Override
	public void setAttribute(String name, Object value) {
		throw new IllegalStateException();
	}

	@Override
	public void removeAttribute(String name) {
		throw new IllegalStateException();
	}

	@Override
	public void invalidate() {
		throw new IllegalStateException();
	}
}
