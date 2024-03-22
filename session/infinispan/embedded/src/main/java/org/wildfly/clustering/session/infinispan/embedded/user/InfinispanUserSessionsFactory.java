/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded.user;

import static org.wildfly.clustering.cache.function.Functions.constantFunction;
import static org.wildfly.common.function.Functions.discardingConsumer;

import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import org.infinispan.Cache;
import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheEntryMutator;
import org.wildfly.clustering.session.cache.user.MutableUserSessions;
import org.wildfly.clustering.session.cache.user.UserSessionsFactory;
import org.wildfly.clustering.session.user.UserSessions;

/**
 * @param <D> the deployment type
 * @param <S> the session type
 * @author Paul Ferraro
 */
public class InfinispanUserSessionsFactory<D, S> implements UserSessionsFactory<Map<D, S>, D, S> {

	private final Cache<UserSessionsKey, Map<D, S>> cache;
	private final Cache<UserSessionsKey, Map<D, S>> writeOnlyCache;

	public InfinispanUserSessionsFactory(EmbeddedCacheConfiguration configuration) {
		this.cache = configuration.getCache();
		this.writeOnlyCache = configuration.getWriteOnlyCache();
	}

	@Override
	public UserSessions<D, S> createUserSessions(String id, Map<D, S> value) {
		UserSessionsKey key = new UserSessionsKey(id);
		CacheEntryMutator mutator = new EmbeddedCacheEntryMutator<>(this.cache, key, value);
		return new MutableUserSessions<>(value, mutator);
	}

	@Override
	public CompletionStage<Map<D, S>> createValueAsync(String id, Void context) {
		Map<D, S> sessions = new ConcurrentHashMap<>();
		return this.writeOnlyCache.putAsync(new UserSessionsKey(id), sessions).thenApply(constantFunction(sessions));
	}

	@Override
	public CompletionStage<Map<D, S>> findValueAsync(String id) {
		return this.cache.getAsync(new UserSessionsKey(id));
	}

	@Override
	public CompletionStage<Void> removeAsync(String id) {
		return this.writeOnlyCache.removeAsync(new UserSessionsKey(id)).thenAccept(discardingConsumer());
	}
}
