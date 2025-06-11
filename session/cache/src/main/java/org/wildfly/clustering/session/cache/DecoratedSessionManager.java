/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionStatistics;

/**
 * A session manager that delegates to another session manager.
 * @author Paul Ferraro
 * @param <C> the session context type
 */
public class DecoratedSessionManager<C> implements SessionManager<C> {

	private final SessionManager<C> manager;

	public DecoratedSessionManager(SessionManager<C> manager) {
		this.manager = manager;
	}

	@Override
	public Supplier<Batch> getBatchFactory() {
		return this.manager.getBatchFactory();
	}

	@Override
	public Supplier<String> getIdentifierFactory() {
		return this.manager.getIdentifierFactory();
	}

	@Override
	public boolean isStarted() {
		return this.manager.isStarted();
	}

	@Override
	public void start() {
		this.manager.start();
	}

	@Override
	public void stop() {
		this.manager.stop();
	}

	@Override
	public CompletionStage<Session<C>> createSessionAsync(String id) {
		return this.manager.createSessionAsync(id);
	}

	@Override
	public CompletionStage<Session<C>> findSessionAsync(String id) {
		return this.manager.findSessionAsync(id);
	}

	@Override
	public CompletionStage<ImmutableSession> findImmutableSessionAsync(String id) {
		return this.manager.findImmutableSessionAsync(id);
	}

	@Override
	public Session<C> getDetachedSession(String id) {
		return this.manager.getDetachedSession(id);
	}

	@Override
	public SessionStatistics getStatistics() {
		return this.manager.getStatistics();
	}
}
