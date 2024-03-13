/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.Map;
import java.util.function.Supplier;

import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionMetaData;
import org.wildfly.common.function.Functions;

/**
 * A generic decorated session.
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public class DecoratedSession<C> implements Session<C>, Supplier<Session<C>> {

	private final Supplier<Session<C>> reference;

	public DecoratedSession(Session<C> session) {
		this(Functions.constantSupplier(session));
	}

	public DecoratedSession(Supplier<Session<C>> reference) {
		this.reference = reference;
	}

	@Override
	public Session<C> get() {
		return this.reference.get();
	}

	@Override
	public String getId() {
		return this.reference.get().getId();
	}

	@Override
	public C getContext() {
		return this.reference.get().getContext();
	}

	@Override
	public SessionMetaData getMetaData() {
		return this.reference.get().getMetaData();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return this.reference.get().getAttributes();
	}

	@Override
	public boolean isValid() {
		return this.reference.get().isValid();
	}

	@Override
	public void invalidate() {
		this.reference.get().invalidate();
	}

	@Override
	public void close() {
		this.reference.get().close();
	}

	@Override
	public int hashCode() {
		return this.reference.get().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return this.reference.get().equals(object);
	}

	@Override
	public String toString() {
		return this.reference.get().toString();
	}
}
