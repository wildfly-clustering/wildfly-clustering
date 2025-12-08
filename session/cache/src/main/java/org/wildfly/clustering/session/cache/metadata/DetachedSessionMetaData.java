/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
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

	/**
	 * Creates detached session metadata using the specified session factory.
	 * @param batchFactory a batch factory
	 * @param sessionFactory a session factory
	 */
	public DetachedSessionMetaData(Supplier<B> batchFactory, Supplier<Session<C>> sessionFactory) {
		this.batchFactory = batchFactory;
		this.sessionFactory = sessionFactory;
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
	public Optional<Instant> getLastAccessStartTime() {
		try (B batch = this.batchFactory.get()) {
			try (Session<C> session = this.sessionFactory.get()) {
				return session.getMetaData().getLastAccessStartTime();
			}
		}
	}

	@Override
	public Optional<Instant> getLastAccessEndTime() {
		try (B batch = this.batchFactory.get()) {
			try (Session<C> session = this.sessionFactory.get()) {
				return session.getMetaData().getLastAccessEndTime();
			}
		}
	}

	@Override
	public Optional<Duration> getMaxIdle() {
		try (B batch = this.batchFactory.get()) {
			try (Session<C> session = this.sessionFactory.get()) {
				return session.getMetaData().getMaxIdle();
			}
		}
	}

	@Override
	public void setMaxIdle(Duration duration) {
		try (B batch = this.batchFactory.get()) {
			try (Session<C> session = this.sessionFactory.get()) {
				session.getMetaData().setMaxIdle(duration);
			}
		}
	}

	@Override
	public void setLastAccess(Instant startTime, Instant endTime) {
		// Not relevant to detached sessions
		throw new IllegalStateException();
	}
}
