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
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.session.cache.user.UserContext;
import org.wildfly.clustering.session.cache.user.UserContextEntry;
import org.wildfly.clustering.session.cache.user.UserContextFactory;

/**
 * The factory for creating the context of a user.
 * @param <PC> the persistent context type
 * @param <PV> the marshalled persistent context type
 * @param <TC> the transient context type
 * @author Paul Ferraro
 */
public class InfinispanUserContextFactory<PC, PV, TC> implements UserContextFactory<UserContext<PV, TC>, PC, TC> {

	private final Cache<UserContextKey, UserContext<PV, TC>> findCache;
	private final Cache<UserContextKey, UserContext<PV, TC>> writeCache;
	private final Marshaller<PC, PV> marshaller;
	private final Supplier<TC> contextFactory;

	/**
	 * Creates a user context factory
	 * @param configuration the configuration for the associated cache
	 * @param marshaller a marshaller for the persistent context of a user
	 * @param contextFactory a factory for creating the user context
	 */
	public InfinispanUserContextFactory(EmbeddedCacheConfiguration configuration, Marshaller<PC, PV> marshaller, Supplier<TC> contextFactory) {
		this.writeCache = configuration.getWriteOnlyCache();
		this.findCache = configuration.getReadForUpdateCache();
		this.marshaller = marshaller;
		this.contextFactory = contextFactory;
	}

	@Override
	public CompletionStage<UserContext<PV, TC>> createValueAsync(String id, PC context) {
		try {
			UserContext<PV, TC> entry = new UserContextEntry<>(this.marshaller.write(context));
			return this.writeCache.putAsync(new UserContextKey(id), entry).thenApply(Function.of(entry));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public CompletionStage<UserContext<PV, TC>> findValueAsync(String id) {
		return this.findCache.getAsync(new UserContextKey(id));
	}

	@Override
	public CompletionStage<Void> removeAsync(String id) {
		return this.writeCache.removeAsync(new UserContextKey(id)).thenAccept(Consumer.of());
	}

	@Override
	public Map.Entry<PC, TC> createUserContext(UserContext<PV, TC> entry) {
		try {
			PC context = this.marshaller.read(entry.getPersistentContext());
			return new AbstractMap.SimpleImmutableEntry<>(context, entry.getTransientContext().get(this.contextFactory));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
