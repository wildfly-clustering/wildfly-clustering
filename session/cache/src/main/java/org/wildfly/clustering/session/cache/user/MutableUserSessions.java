/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache.user;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.server.util.BlockingReferenceMap;
import org.wildfly.clustering.session.user.UserSessions;

/**
 * A mutable user sessions implementation.
 * @param <K> the cache key
 * @param <D> the deployment type
 * @param <S> the session type
 */
public class MutableUserSessions<K, D, S> implements UserSessions<D, S> {

	private final K key;
	private final Map<D, S> sessions;
	private final CacheEntryMutatorFactory<K, Map<D, S>> mutatorFactory;
	private final BlockingReferenceMap<D, S> updates = BlockingReferenceMap.of(new TreeMap<>());

	/**
	 * Creates a mutable user sessions.
	 * @param key the cache key
	 * @param sessions a map of session identifiers per deployment
	 * @param mutatorFactory a cache entry mutator factory
	 */
	public MutableUserSessions(K key, Map<D, S> sessions, CacheEntryMutatorFactory<K, Map<D, S>> mutatorFactory) {
		this.key = key;
		this.sessions = sessions;
		this.mutatorFactory = mutatorFactory;
	}

	@Override
	public Set<D> getDeployments() {
		return Collections.unmodifiableSet(this.sessions.keySet());
	}

	@Override
	public S getSession(D deployment) {
		return this.sessions.get(deployment);
	}

	@Override
	public S removeSession(D deployment) {
		S removed = this.sessions.remove(deployment);
		if (removed != null) {
			this.updates.reference(deployment).writer(Supplier.empty()).get();
		}
		return removed;
	}

	@Override
	public boolean addSession(D deployment, S session) {
		boolean added = this.sessions.put(deployment, session) == null;
		if (added) {
			this.updates.reference(deployment).writer(session).get();
		}
		return added;
	}

	@Override
	public void close() {
		this.updates.reader().consume(map -> {
			if (!map.isEmpty()) {
				this.mutatorFactory.createMutator(this.key, map).run();
			}
		});
	}
}
