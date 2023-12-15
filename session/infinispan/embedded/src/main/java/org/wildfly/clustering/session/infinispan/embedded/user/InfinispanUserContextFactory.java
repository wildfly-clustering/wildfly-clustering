/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded.user;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import org.infinispan.Cache;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.session.cache.user.UserContext;
import org.wildfly.clustering.session.cache.user.UserContextEntry;
import org.wildfly.clustering.session.cache.user.UserContextFactory;
import org.wildfly.common.function.Functions;

/**
 * @author Paul Ferraro
 */
public class InfinispanUserContextFactory<C, CV, T> implements UserContextFactory<UserContext<CV, T>, C, T> {

	private final Cache<UserContextKey, UserContext<CV, T>> findCache;
	private final Cache<UserContextKey, UserContext<CV, T>> writeCache;
	private final Marshaller<C, CV> marshaller;
	private final Supplier<T> contextFactory;

	public InfinispanUserContextFactory(EmbeddedCacheConfiguration configuration, Marshaller<C, CV> marshaller, Supplier<T> contextFactory) {
		this.writeCache = configuration.getWriteOnlyCache();
		this.findCache = configuration.getReadForUpdateCache();
		this.marshaller = marshaller;
		this.contextFactory = contextFactory;
	}

	@Override
	public CompletionStage<UserContext<CV, T>> createValueAsync(String id, C context) {
		try {
			UserContext<CV, T> entry = new UserContextEntry<>(this.marshaller.write(context));
			return this.writeCache.putAsync(new UserContextKey(id), entry).thenApply(v -> entry);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public CompletionStage<UserContext<CV, T>> findValueAsync(String id) {
		return this.findCache.getAsync(new UserContextKey(id));
	}

	@Override
	public CompletionStage<Void> removeAsync(String id) {
		return this.writeCache.removeAsync(new UserContextKey(id)).thenAccept(Functions.discardingConsumer());
	}

	@Override
	public Map.Entry<C, T> createUserContext(UserContext<CV, T> entry) {
		try {
			C context = this.marshaller.read(entry.getPersistentContext());
			return new AbstractMap.SimpleImmutableEntry<>(context, entry.getTransientContext().get(this.contextFactory));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
