/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.user;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import org.infinispan.client.hotrod.RemoteCache;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.cache.function.MapComputeFunction;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.session.cache.user.MutableUserSessions;
import org.wildfly.clustering.session.cache.user.UserSessionsFactory;
import org.wildfly.clustering.session.user.UserSessions;

/**
 * A user sessions factory.
 * @param <D> the deployment type
 * @param <S> the session type
 * @author Paul Ferraro
 */
public class HotRodUserSessionsFactory<D, S> implements UserSessionsFactory<Map<D, S>, D, S> {

	private final RemoteCache<UserSessionsKey, Map<D, S>> readCache;
	private final RemoteCache<UserSessionsKey, Map<D, S>> writeCache;
	private final CacheEntryMutatorFactory<UserSessionsKey, Map<D, S>> mutatorFactory;

	/**
	 * Creates a user sessions factory
	 * @param configuration the configuration associated with the cache
	 */
	public HotRodUserSessionsFactory(RemoteCacheConfiguration configuration) {
		this.readCache = configuration.getCache();
		this.writeCache = configuration.getIgnoreReturnCache();
		this.mutatorFactory = configuration.getCacheEntryMutatorFactory(MapComputeFunction::new);
	}

	@Override
	public UserSessions<D, S> createUserSessions(String id, Map<D, S> value) {
		return new MutableUserSessions<>(new UserSessionsKey(id), value, this.mutatorFactory);
	}

	@Override
	public Map<D, S> createValue(String id, Void context) {
		return new ConcurrentHashMap<>();
	}

	@Override
	public CompletionStage<Map<D, S>> createValueAsync(String id, Void context) {
		return CompletableFuture.completedStage(this.createValue(id, context));
	}

	@Override
	public CompletionStage<Map<D, S>> findValueAsync(String id) {
		return this.readCache.getAsync(new UserSessionsKey(id)).thenApply(sessions -> (sessions != null) ? sessions : this.createValue(id, null));
	}

	@Override
	public CompletionStage<Void> removeAsync(String id) {
		return this.writeCache.removeAsync(new UserSessionsKey(id)).thenAccept(Consumer.empty());
	}
}
