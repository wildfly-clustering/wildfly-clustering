/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache.user;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.cache.BiCacheEntryCreator;
import org.wildfly.clustering.cache.BiCacheEntryLocator;
import org.wildfly.clustering.cache.CacheEntryRemover;
import org.wildfly.clustering.cache.CacheProperties;
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
	CacheProperties getCacheProperties();

	User<C, T, D, S> createUser(String id, Map.Entry<CV, SV> value);

	@Override
	default Map.Entry<CompletionStage<CV>, CompletionStage<SV>> createEntry(String id, C context) {
		return Map.entry(this.getUserContextFactory().createValueAsync(id, context), this.getUserSessionsFactory().createValueAsync(id, null));
	}

	@Override
	default Map.Entry<CompletionStage<CV>, CompletionStage<SV>> findEntry(String id) {
		CompletionStage<CV> contextStage = this.getUserContextFactory().findValueAsync(id);
		// If cache locks on read, find meta data first
		CompletionStage<SV> sessionsStage = this.getCacheProperties().isLockOnRead() ? contextStage.thenCompose(metaData -> (metaData != null) ? this.getUserSessionsFactory().findValueAsync(id) : CompletableFuture.completedStage(null)) : this.getUserSessionsFactory().findValueAsync(id);
		return Map.entry(contextStage, sessionsStage);
	}

	@Override
	default Map.Entry<CompletionStage<CV>, CompletionStage<SV>> tryEntry(String id) {
		CompletionStage<CV> contextStage = this.getUserContextFactory().tryValueAsync(id);
		// If cache locks on read, find meta data first
		CompletionStage<SV> sessionsStage = this.getCacheProperties().isLockOnRead() ? contextStage.thenCompose(metaData -> (metaData != null) ? this.getUserSessionsFactory().tryValueAsync(id) : CompletableFuture.completedStage(null)) : this.getUserSessionsFactory().tryValueAsync(id);
		return Map.entry(contextStage, sessionsStage);
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
