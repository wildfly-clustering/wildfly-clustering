/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.user;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.session.cache.user.UserContext;
import org.wildfly.clustering.session.cache.user.UserContextEntry;
import org.wildfly.clustering.session.cache.user.UserContextFactory;
import org.wildfly.common.function.Functions;

/**
 * @author Paul Ferraro
 */
public class HotRodUserContextFactory<C, CV, L> implements UserContextFactory<UserContext<CV, L>, C, L> {

	private final RemoteCache<UserContextKey, UserContext<CV, L>> cache;
	private final Flag[] ignoreReturnFlags;
	private final Marshaller<C, CV> marshaller;
	private final Supplier<L> localContextFactory;

	public HotRodUserContextFactory(RemoteCacheConfiguration configuration, Marshaller<C, CV> marshaller, Supplier<L> localContextFactory) {
		this.cache = configuration.getCache();
		this.ignoreReturnFlags = configuration.getIgnoreReturnFlags();
		this.marshaller = marshaller;
		this.localContextFactory = localContextFactory;
	}

	@Override
	public CompletionStage<UserContext<CV, L>> createValueAsync(String id, C context) {
		try {
			UserContext<CV, L> entry = new UserContextEntry<>(this.marshaller.write(context));
			return this.cache.withFlags(this.ignoreReturnFlags).putAsync(new UserContextKey(id), entry).thenApply(v -> entry);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public CompletionStage<UserContext<CV, L>> findValueAsync(String id) {
		return this.cache.getAsync(new UserContextKey(id));
	}

	@Override
	public CompletionStage<Void> removeAsync(String id) {
		return this.cache.withFlags(this.ignoreReturnFlags).removeAsync(new UserContextKey(id)).thenAccept(Functions.discardingConsumer());
	}

	@Override
	public Map.Entry<C, L> createUserContext(UserContext<CV, L> entry) {
		try {
			C context = this.marshaller.read(entry.getContext());
			return new AbstractMap.SimpleImmutableEntry<>(context, entry.getContext(this.localContextFactory));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
