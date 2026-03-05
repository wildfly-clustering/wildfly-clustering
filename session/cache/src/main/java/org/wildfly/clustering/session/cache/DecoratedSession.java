/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.Map;

import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionMetaData;

/**
 * A session decorator.
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public class DecoratedSession<C> implements Session<C> {

	private final Session<C> session;

	/**
	 * Creates a session decorator.
	 * @param session the decorated session
	 */
	public DecoratedSession(Session<C> session) {
		this.session = session;
	}

	@Override
	public String getId() {
		return this.session.getId();
	}

	@Override
	public C getContext() {
		return this.session.getContext();
	}

	@Override
	public SessionMetaData getMetaData() {
		return this.session.getMetaData();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return this.session.getAttributes();
	}

	@Override
	public boolean isValid() {
		return this.session.isValid();
	}

	@Override
	public void invalidate() {
		this.session.invalidate();
	}

	@Override
	public void close() {
		this.session.close();
	}

	@Override
	public int hashCode() {
		return this.session.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return this.session.equals(object);
	}

	@Override
	public String toString() {
		return this.session.toString();
	}
}
