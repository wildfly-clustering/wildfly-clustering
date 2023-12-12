/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import org.wildfly.clustering.cache.batch.Batch;

/**
 * Out-of-band session implementation, for use outside the context of a request.
 * @author Paul Ferraro
 * @param <C> the session context
 * @param <B> the batch type
 */
public class OOBSession<C, B extends Batch> implements Session<C>, SessionMetaData, SessionAttributes {

	private final SessionManager<C, B> manager;
	private final String id;
	private final C context;

	public OOBSession(SessionManager<C, B> manager, String id, C context) {
		this.manager = manager;
		this.id = id;
		this.context = context;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public SessionMetaData getMetaData() {
		return this;
	}

	@Override
	public SessionAttributes getAttributes() {
		return this;
	}

	@Override
	public C getContext() {
		return this.context;
	}

	@Override
	public void close() {
		// OOB session has no lifecycle
	}

	@Override
	public boolean isNew() {
		return false;
	}

	@Override
	public boolean isValid() {
		try (B batch = this.manager.getBatcher().createBatch()) {
			return this.manager.findImmutableSession(this.id) != null;
		}
	}

	@Override
	public void invalidate() {
		try (B batch = this.manager.getBatcher().createBatch()) {
			try (Session<C> session = this.getSession()) {
				session.invalidate();
			}
		}
	}

	@Override
	public boolean isExpired() {
		try (B batch = this.manager.getBatcher().createBatch()) {
			try (Session<C> session = this.getSession()) {
				return session.getMetaData().isExpired();
			}
		}
	}

	@Override
	public Instant getCreationTime() {
		try (B batch = this.manager.getBatcher().createBatch()) {
			try (Session<C> session = this.getSession()) {
				return session.getMetaData().getCreationTime();
			}
		}
	}

	@Override
	public Instant getLastAccessStartTime() {
		try (B batch = this.manager.getBatcher().createBatch()) {
			try (Session<C> session = this.getSession()) {
				return session.getMetaData().getLastAccessStartTime();
			}
		}
	}

	@Override
	public Instant getLastAccessEndTime() {
		try (B batch = this.manager.getBatcher().createBatch()) {
			try (Session<C> session = this.getSession()) {
				return session.getMetaData().getLastAccessEndTime();
			}
		}
	}

	@Override
	public Duration getTimeout() {
		try (B batch = this.manager.getBatcher().createBatch()) {
			try (Session<C> session = this.getSession()) {
				return session.getMetaData().getTimeout();
			}
		}
	}

	@Override
	public void setLastAccess(Instant startTime, Instant endTime) {
		throw new IllegalStateException();
	}

	@Override
	public void setTimeout(Duration duration) {
		try (B batch = this.manager.getBatcher().createBatch()) {
			try (Session<C> session = this.getSession()) {
				session.getMetaData().setTimeout(duration);
			}
		}
	}

	@Override
	public Set<String> getAttributeNames() {
		try (B batch = this.manager.getBatcher().createBatch()) {
			try (Session<C> session = this.getSession()) {
				return session.getAttributes().getAttributeNames();
			}
		}
	}

	@Override
	public Object getAttribute(String name) {
		try (B batch = this.manager.getBatcher().createBatch()) {
			try (Session<C> session = this.getSession()) {
				return session.getAttributes().getAttribute(name);
			}
		}
	}

	@Override
	public Object removeAttribute(String name) {
		try (B batch = this.manager.getBatcher().createBatch()) {
			try (Session<C> session = this.getSession()) {
				return session.getAttributes().removeAttribute(name);
			}
		}
	}

	@Override
	public Object setAttribute(String name, Object value) {
		try (B batch = this.manager.getBatcher().createBatch()) {
			try (Session<C> session = this.getSession()) {
				return session.getAttributes().setAttribute(name, value);
			}
		}
	}

	private Session<C> getSession() {
		return Optional.ofNullable(this.manager.findSession(this.id)).orElseThrow(IllegalStateException::new);
	}
}
