/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache.user;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.cache.BiCacheEntryCreator;
import org.wildfly.clustering.cache.BiCacheEntryLocator;
import org.wildfly.clustering.cache.CacheEntryRemover;
import org.wildfly.clustering.session.user.User;

/**
 * Creates an {@link User} from its cache storage value.
 * @author Paul Ferraro
 * @param <CV> the user context value type
 * @param <C> the persistent context type
 * @param <T> the transient context type
 * @param <SV> the user sessions value type
 * @param <D> the deployment type
 * @param <S> the session type
 */
public interface UserFactory<CV, C, T, SV, D, S> extends BiCacheEntryCreator<String, CV, SV, C>, BiCacheEntryLocator<String, CV, SV>, CacheEntryRemover<String> {

	UserContextFactory<CV, C, T> getUserContextFactory();
	UserSessionsFactory<SV, D, S> getUserSessionsFactory();

	User<C, T, D, S> createUser(String id, Map.Entry<CV, SV> value);

	default CompletionStage<User<C, T, D, S>> createUserAsync(String id, Map.Entry<CompletionStage<CV>, CompletionStage<SV>> entry) {
		return entry.getKey().thenCombine(entry.getValue(), AbstractMap.SimpleImmutableEntry::new).thenApply(value -> this.createUser(id, value));
	}

	@Override
	default Map.Entry<CompletionStage<CV>, CompletionStage<SV>> createEntry(String id, C context) {
		return Map.entry(this.getUserContextFactory().createValueAsync(id, context), this.getUserSessionsFactory().createValueAsync(id, null));
	}

	@Override
	default Map.Entry<CompletionStage<CV>, CompletionStage<SV>> findEntry(String id) {
		return Map.entry(this.getUserContextFactory().findValueAsync(id), this.getUserSessionsFactory().findValueAsync(id));
	}

	@Override
	default Map.Entry<CompletionStage<CV>, CompletionStage<SV>> tryEntry(String id) {
		return Map.entry(this.getUserContextFactory().tryValueAsync(id), this.getUserSessionsFactory().tryValueAsync(id));
	}

	@Override
	default CompletionStage<Void> removeAsync(String id) {
		return CompletableFuture.allOf(this.getUserContextFactory().removeAsync(id).toCompletableFuture(), this.getUserSessionsFactory().removeAsync(id).toCompletableFuture());
	}

	@Override
	default CompletionStage<Void> purgeAsync(String id) {
		return CompletableFuture.allOf(this.getUserContextFactory().purgeAsync(id).toCompletableFuture(), this.getUserSessionsFactory().purgeAsync(id).toCompletableFuture());
	}
}
