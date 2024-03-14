/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionMetaData;

/**
 * {@link SessionMetaData} implementation for detached sessions.
 * @param <C> the session context type
 * @param <B> the batch type
 * @author Paul Ferraro
 */
public class DetachedSessionMetaData<C, B extends Batch> implements SessionMetaData {
	private final Supplier<B> batchFactory;
	private final Supplier<Session<C>> sessionFactory;

	public DetachedSessionMetaData(Supplier<B> batchFactory, Supplier<Session<C>> sessionFactory) {
		this.batchFactory = batchFactory;
		this.sessionFactory = sessionFactory;
	}

	@Override
	public boolean isNew() {
		// A detached session is not new, by definition.
		return false;
	}

	@Override
	public boolean isExpired() {
		try (B batch = this.batchFactory.get()) {
			try (Session<C> session = this.sessionFactory.get()) {
				return session.getMetaData().isExpired();
			}
		}
	}

	@Override
	public Instant getCreationTime() {
		try (B batch = this.batchFactory.get()) {
			try (Session<C> session = this.sessionFactory.get()) {
				return session.getMetaData().getCreationTime();
			}
		}
	}

	@Override
	public Instant getLastAccessStartTime() {
		try (B batch = this.batchFactory.get()) {
			try (Session<C> session = this.sessionFactory.get()) {
				return session.getMetaData().getLastAccessStartTime();
			}
		}
	}

	@Override
	public Instant getLastAccessEndTime() {
		try (B batch = this.batchFactory.get()) {
			try (Session<C> session = this.sessionFactory.get()) {
				return session.getMetaData().getLastAccessEndTime();
			}
		}
	}

	@Override
	public Duration getTimeout() {
		try (B batch = this.batchFactory.get()) {
			try (Session<C> session = this.sessionFactory.get()) {
				return session.getMetaData().getTimeout();
			}
		}
	}

	@Override
	public void setTimeout(Duration duration) {
		try (B batch = this.batchFactory.get()) {
			try (Session<C> session = this.sessionFactory.get()) {
				session.getMetaData().setTimeout(duration);
			}
		}
	}

	@Override
	public void setLastAccess(Instant startTime, Instant endTime) {
		// Not relevant to detached sessions
		throw new IllegalStateException();
	}
}
