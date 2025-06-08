/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.user;

import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import org.infinispan.client.hotrod.RemoteCache;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.session.cache.user.MutableUserSessions;
import org.wildfly.clustering.session.cache.user.UserSessionsFactory;
import org.wildfly.clustering.session.user.UserSessions;

/**
 * @param <D> the deployment type
 * @param <S> the session type
 * @author Paul Ferraro
 */
public class HotRodUserSessionsFactory<D, S> implements UserSessionsFactory<Map<D, S>, D, S> {

	private final RemoteCache<UserSessionsKey, Map<D, S>> readCache;
	private final RemoteCache<UserSessionsKey, Map<D, S>> writeCache;
	private final CacheEntryMutatorFactory<UserSessionsKey, Map<D, S>> mutatorFactory;

	public HotRodUserSessionsFactory(RemoteCacheConfiguration configuration) {
		this.readCache = configuration.getCache();
		this.writeCache = configuration.getIgnoreReturnCache();
		this.mutatorFactory = configuration.getCacheEntryMutatorFactory();
	}

	@Override
	public UserSessions<D, S> createUserSessions(String id, Map<D, S> value) {
		UserSessionsKey key = new UserSessionsKey(id);
		Runnable mutator = this.mutatorFactory.createMutator(key, value);
		return new MutableUserSessions<>(value, mutator);
	}

	@Override
	public CompletionStage<Map<D, S>> createValueAsync(String id, Void context) {
		Map<D, S> sessions = new ConcurrentHashMap<>();
		return this.writeCache.putAsync(new UserSessionsKey(id), sessions).thenApply(Function.of(sessions));
	}

	@Override
	public CompletionStage<Map<D, S>> findValueAsync(String id) {
		return this.readCache.getAsync(new UserSessionsKey(id));
	}

	@Override
	public CompletionStage<Void> removeAsync(String id) {
		return this.writeCache.removeAsync(new UserSessionsKey(id)).thenAccept(Consumer.empty());
	}
}
