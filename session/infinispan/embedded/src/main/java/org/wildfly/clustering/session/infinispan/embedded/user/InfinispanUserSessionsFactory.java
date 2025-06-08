/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded.user;

import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import org.infinispan.Cache;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
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
public class InfinispanUserSessionsFactory<D, S> implements UserSessionsFactory<Map<D, S>, D, S> {

	private final Cache<UserSessionsKey, Map<D, S>> cache;
	private final Cache<UserSessionsKey, Map<D, S>> writeOnlyCache;
	private final CacheEntryMutatorFactory<UserSessionsKey, Map<D, S>> mutatorFactory;

	public InfinispanUserSessionsFactory(EmbeddedCacheConfiguration configuration) {
		this.cache = configuration.getCache();
		this.writeOnlyCache = configuration.getWriteOnlyCache();
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
		return this.writeOnlyCache.putAsync(new UserSessionsKey(id), sessions).thenApply(Function.of(sessions));
	}

	@Override
	public CompletionStage<Map<D, S>> findValueAsync(String id) {
		return this.cache.getAsync(new UserSessionsKey(id));
	}

	@Override
	public CompletionStage<Void> removeAsync(String id) {
		return this.writeOnlyCache.removeAsync(new UserSessionsKey(id)).thenAccept(Consumer.empty());
	}
}
