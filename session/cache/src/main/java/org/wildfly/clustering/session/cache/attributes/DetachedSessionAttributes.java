/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.session.Session;

/**
 * A {@link SessionAttributes} implementation for detached sessions.
 * @param <C> the session context type
 * @param <B> the batch type
 * @author Paul Ferraro
 */
public class DetachedSessionAttributes<C, B extends Batch> implements SessionAttributes {
	private final Supplier<B> batchFactory;
	private final Supplier<Session<C>> sessionFactory;

	public DetachedSessionAttributes(Supplier<B> batchFactory, Supplier<Session<C>> sessionFactory) {
		this.batchFactory = batchFactory;
		this.sessionFactory = sessionFactory;
	}

	@Override
	public Set<String> keySet() {
		try (B batch = this.batchFactory.get()) {
			try (Session<C> session = this.sessionFactory.get()) {
				return session.getAttributes().keySet();
			}
		}
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		try (B batch = this.batchFactory.get()) {
			try (Session<C> session = this.sessionFactory.get()) {
				return session.getAttributes().entrySet();
			}
		}
	}

	@Override
	public Collection<Object> values() {
		try (B batch = this.batchFactory.get()) {
			try (Session<C> session = this.sessionFactory.get()) {
				return session.getAttributes().values();
			}
		}
	}

	@Override
	public Object get(Object name) {
		try (B batch = this.batchFactory.get()) {
			try (Session<C> session = this.sessionFactory.get()) {
				return session.getAttributes().get(name);
			}
		}
	}

	@Override
	public Object put(String name, Object value) {
		try (B batch = this.batchFactory.get()) {
			try (Session<C> session = this.sessionFactory.get()) {
				return session.getAttributes().put(name, value);
			}
		}
	}

	@Override
	public Object remove(Object key) {
		try (B batch = this.batchFactory.get()) {
			try (Session<C> session = this.sessionFactory.get()) {
				return session.getAttributes().remove(key);
			}
		}
	}

	@Override
	public void close() {
		// A detached session has no lifecycle
	}
}
