/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionMetaData;
import org.wildfly.clustering.session.cache.attributes.DetachedSessionAttributes;
import org.wildfly.clustering.session.cache.metadata.DetachedSessionMetaData;

/**
 * Detached session implementation, for use outside the context of a request.
 * @author Paul Ferraro
 * @param <C> the session context
 * @param <B> the batch type
 */
public class DetachedSession<C, B extends Batch> extends AbstractImmutableSession implements Session<C> {

	private final SessionManager<C, B> manager;
	private final C context;
	private final SessionMetaData metaData;
	private final Map<String, Object> attributes;

	public DetachedSession(SessionManager<C, B> manager, String id, C context) {
		super(id);
		this.manager = manager;
		this.context = context;
		Supplier<B> batchFactory = this::getBatch;
		Supplier<Session<C>> sessionFactory = this::getSession;
		this.metaData = new DetachedSessionMetaData<>(batchFactory, sessionFactory);
		this.attributes = new DetachedSessionAttributes<>(batchFactory, sessionFactory);
	}

	@Override
	public SessionMetaData getMetaData() {
		return this.metaData;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return this.attributes;
	}

	@Override
	public C getContext() {
		return this.context;
	}

	@Override
	public boolean isValid() {
		try (B batch = this.getBatch()) {
			return this.manager.findImmutableSession(this.getId()) != null;
		}
	}

	@Override
	public void invalidate() {
		try (B batch = this.getBatch()) {
			try (Session<C> session = this.getSession()) {
				session.invalidate();
			}
		}
	}

	@Override
	public void close() {
		// A detached session has no lifecycle
	}

	private Session<C> getSession() {
		return Optional.ofNullable(this.manager.findSession(this.getId())).orElseThrow(IllegalStateException::new);
	}

	private B getBatch() {
		return this.manager.getBatcher().createBatch();
	}
}
