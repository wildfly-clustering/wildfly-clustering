/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache.user;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.cache.BiCreator;
import org.wildfly.clustering.cache.BiLocator;
import org.wildfly.clustering.cache.Remover;
import org.wildfly.clustering.session.user.User;

/**
 * Creates an {@link User} from its cache storage value.
 * @author Paul Ferraro
 * @param <V> the cache value type
 */
public interface UserFactory<CV, C, L, SV, D, S> extends BiCreator<String, CV, SV, C>, BiLocator<String, CV, SV>, Remover<String> {

	UserContextFactory<CV, C, L> getUserContextFactory();
	UserSessionsFactory<SV, D, S> getUserSessionsFactory();

	User<C, L, D, S> createUser(String id, Map.Entry<CV, SV> value);

	default CompletionStage<User<C, L, D, S>> createUserAsync(String id, Map.Entry<CompletionStage<CV>, CompletionStage<SV>> entry) {
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
