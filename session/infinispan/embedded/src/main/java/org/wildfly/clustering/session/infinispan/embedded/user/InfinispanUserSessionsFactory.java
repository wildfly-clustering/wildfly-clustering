/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded.user;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import org.infinispan.Cache;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.cache.function.MapComputeFunction;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.session.cache.user.MutableUserSessions;
import org.wildfly.clustering.session.cache.user.UserSessionsFactory;
import org.wildfly.clustering.session.user.UserSessions;

/**
 * The factory for creating user sessions.
 * @param <D> the deployment type
 * @param <S> the session type
 * @author Paul Ferraro
 */
public class InfinispanUserSessionsFactory<D, S> implements UserSessionsFactory<Map<D, S>, D, S> {

	private final Cache<UserSessionsKey, Map<D, S>> cache;
	private final Cache<UserSessionsKey, Map<D, S>> writeOnlyCache;
	private final CacheEntryMutatorFactory<UserSessionsKey, Map<D, S>> mutatorFactory;

	/**
	 * Creates a factory for creating user sessions.
	 * @param configuration the configuration associated with the cache
	 */
	public InfinispanUserSessionsFactory(EmbeddedCacheConfiguration configuration) {
		this.cache = configuration.getReadForUpdateCache();
		this.writeOnlyCache = configuration.getWriteOnlyCache();
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
		return this.cache.getAsync(new UserSessionsKey(id)).thenApply(sessions -> (sessions != null) ? sessions : this.createValue(id, null));
	}

	@Override
	public CompletionStage<Void> removeAsync(String id) {
		return this.writeOnlyCache.removeAsync(new UserSessionsKey(id)).thenAccept(Consumer.empty());
	}
}
