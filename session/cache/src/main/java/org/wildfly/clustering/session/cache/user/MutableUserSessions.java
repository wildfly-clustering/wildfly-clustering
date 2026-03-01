/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache.user;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.server.util.BlockingMapReference;
import org.wildfly.clustering.session.user.UserSessions;

/**
 * A mutable user sessions implementation.
 * @param <K> the cache key
 * @param <D> the deployment type
 * @param <S> the session type
 */
public class MutableUserSessions<K, D, S> implements UserSessions<D, S> {

	private final K key;
	private final BlockingMapReference<D, S> sessions;
	private final CacheEntryMutatorFactory<K, Map<D, S>> mutatorFactory;
	// Guarded by sessions
	private final Map<D, S> updates = new TreeMap<>();

	/**
	 * Creates a mutable user sessions.
	 * @param key the cache key
	 * @param sessions a map of session identifiers per deployment
	 * @param mutatorFactory a cache entry mutator factory
	 */
	public MutableUserSessions(K key, Map<D, S> sessions, CacheEntryMutatorFactory<K, Map<D, S>> mutatorFactory) {
		this.key = key;
		this.sessions = BlockingMapReference.of(sessions);
		this.mutatorFactory = mutatorFactory;
	}

	@Override
	public Map<D, S> getSessions() {
		return this.sessions.getReader().map(Map::copyOf).get();
	}

	@Override
	public S getSession(D deployment) {
		return this.sessions.getReference(deployment).getReader().get();
	}

	@Override
	public boolean addSession(D deployment, S session) {
		return this.sessions.getReference(deployment).getWriter(Objects::isNull).getAndSet(() -> {
			this.updates.put(deployment, session);
			return session;
		}) == null;
	}

	@Override
	public S removeSession(D deployment) {
		return this.sessions.getReference(deployment).getWriter(Objects::nonNull).getAndSet(() -> {
			this.updates.put(deployment, null);
			return null;
		});
	}

	@Override
	public void close() {
		this.sessions.getReader().read(sessions -> {
			if (!this.updates.isEmpty()) {
				this.mutatorFactory.createMutator(this.key, this.updates).run();
			}
		});
	}
}
