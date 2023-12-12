/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.user;

import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheEntryMutator;
import org.wildfly.clustering.session.cache.user.MutableUserSessions;
import org.wildfly.clustering.session.cache.user.UserSessionsFactory;
import org.wildfly.clustering.session.user.UserSessions;
import org.wildfly.common.function.Functions;

/**
 * @author Paul Ferraro
 */
public class HotRodUserSessionsFactory<D, S> implements UserSessionsFactory<Map<D, S>, D, S> {

	private final RemoteCache<UserSessionsKey, Map<D, S>> cache;
	private final Flag[] ignoreReturnFlags;

	public HotRodUserSessionsFactory(RemoteCacheConfiguration configuration) {
		this.cache = configuration.getCache();
		this.ignoreReturnFlags = configuration.getIgnoreReturnFlags();
	}

	@Override
	public UserSessions<D, S> createUserSessions(String id, Map<D, S> value) {
		UserSessionsKey key = new UserSessionsKey(id);
		CacheEntryMutator mutator = new RemoteCacheEntryMutator<>(this.cache, this.ignoreReturnFlags, key, value);
		return new MutableUserSessions<>(value, mutator);
	}

	@Override
	public CompletionStage<Map<D, S>> createValueAsync(String id, Void context) {
		Map<D, S> sessions = new ConcurrentHashMap<>();
		return this.cache.withFlags(this.ignoreReturnFlags).putAsync(new UserSessionsKey(id), sessions).thenApply(v -> sessions);
	}

	@Override
	public CompletionStage<Map<D, S>> findValueAsync(String id) {
		return this.cache.getAsync(new UserSessionsKey(id));
	}

	@Override
	public CompletionStage<Void> removeAsync(String id) {
		return this.cache.withFlags(this.ignoreReturnFlags).removeAsync(new UserSessionsKey(id)).thenAccept(Functions.discardingConsumer());
	}
}
