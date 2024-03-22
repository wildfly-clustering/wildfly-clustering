/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded.metadata;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import org.infinispan.Cache;
import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheEntryComputer;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.metadata.InvalidatableSessionMetaData;
import org.wildfly.clustering.session.cache.metadata.SessionMetaDataFactory;
import org.wildfly.clustering.session.cache.metadata.coarse.ContextualSessionMetaDataEntry;
import org.wildfly.clustering.session.cache.metadata.coarse.DefaultImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.metadata.coarse.DefaultSessionMetaData;
import org.wildfly.clustering.session.cache.metadata.coarse.DefaultSessionMetaDataEntry;
import org.wildfly.clustering.session.cache.metadata.coarse.MutableSessionMetaDataEntry;
import org.wildfly.clustering.session.cache.metadata.coarse.MutableSessionMetaDataOffsetValues;
import org.wildfly.clustering.session.cache.metadata.coarse.SessionMetaDataEntryFunction;
import org.wildfly.common.function.Functions;

/**
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public class InfinispanSessionMetaDataFactory<C> implements SessionMetaDataFactory<ContextualSessionMetaDataEntry<C>> {

	private final Cache<SessionMetaDataKey, ContextualSessionMetaDataEntry<C>> cache;
	private final Cache<SessionMetaDataKey, ContextualSessionMetaDataEntry<C>> readForUpdateCache;
	private final Cache<SessionMetaDataKey, ContextualSessionMetaDataEntry<C>> tryReadForUpdateCache;
	private final Cache<SessionMetaDataKey, ContextualSessionMetaDataEntry<C>> writeOnlyCache;
	private final Cache<SessionMetaDataKey, ContextualSessionMetaDataEntry<C>> silentWriteCache;
	private final CacheProperties properties;

	public InfinispanSessionMetaDataFactory(EmbeddedCacheConfiguration configuration) {
		this.cache = configuration.getCache();
		this.readForUpdateCache = configuration.getReadForUpdateCache();
		this.tryReadForUpdateCache = configuration.getTryReadForUpdateCache();
		this.writeOnlyCache = configuration.getWriteOnlyCache();
		this.silentWriteCache = configuration.getSilentWriteCache();
		this.properties = configuration.getCacheProperties();
	}

	@Override
	public CompletionStage<ContextualSessionMetaDataEntry<C>> createValueAsync(String id, Duration defaultTimeout) {
		DefaultSessionMetaDataEntry<C> entry = new DefaultSessionMetaDataEntry<>();
		entry.setTimeout(defaultTimeout);
		return this.writeOnlyCache.putAsync(new SessionMetaDataKey(id), entry).thenApply(v -> entry);
	}

	@Override
	public CompletionStage<ContextualSessionMetaDataEntry<C>> findValueAsync(String id) {
		return this.readForUpdateCache.getAsync(new SessionMetaDataKey(id));
	}

	@Override
	public CompletionStage<ContextualSessionMetaDataEntry<C>> tryValueAsync(String id) {
		return this.tryReadForUpdateCache.getAsync(new SessionMetaDataKey(id));
	}

	@Override
	public CompletionStage<Void> removeAsync(String id) {
		return this.deleteAsync(this.writeOnlyCache, id);
	}

	@Override
	public CompletionStage<Void> purgeAsync(String id) {
		return this.deleteAsync(this.silentWriteCache, id);
	}

	private CompletionStage<Void> deleteAsync(Cache<SessionMetaDataKey, ContextualSessionMetaDataEntry<C>> cache, String id) {
		return cache.removeAsync(new SessionMetaDataKey(id)).thenAccept(Functions.discardingConsumer());
	}

	@Override
	public ImmutableSessionMetaData createImmutableSessionMetaData(String id, ContextualSessionMetaDataEntry<C> entry) {
		return new DefaultImmutableSessionMetaData(entry);
	}

	@Override
	public InvalidatableSessionMetaData createSessionMetaData(String id, ContextualSessionMetaDataEntry<C> entry) {
		MutableSessionMetaDataOffsetValues delta = this.properties.isTransactional() && entry.isNew() ? null : MutableSessionMetaDataOffsetValues.from(entry);
		CacheEntryMutator mutator = (delta != null) ? new EmbeddedCacheEntryComputer<>(this.cache, new SessionMetaDataKey(id), new SessionMetaDataEntryFunction<>(delta)) : CacheEntryMutator.NO_OP;
		return new DefaultSessionMetaData((delta != null) ? new MutableSessionMetaDataEntry(entry, delta) : entry, mutator);
	}

	@Override
	public void close() {
	}
}
