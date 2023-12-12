/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded.metadata;

import java.time.Duration;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheEntryComputeMutator;
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
 * @author Paul Ferraro
 */
public class InfinispanSessionMetaDataFactory<L> implements SessionMetaDataFactory<ContextualSessionMetaDataEntry<L>> {

	private static final Set<Flag> TRY_LOCK_FLAGS = EnumSet.of(Flag.ZERO_LOCK_ACQUISITION_TIMEOUT, Flag.FAIL_SILENTLY);

	private final Cache<SessionMetaDataKey, ContextualSessionMetaDataEntry<L>> cache;
	private final Cache<SessionMetaDataKey, ContextualSessionMetaDataEntry<L>> readForUpdateCache;
	private final Cache<SessionMetaDataKey, ContextualSessionMetaDataEntry<L>> writeOnlyCache;
	private final Cache<SessionMetaDataKey, ContextualSessionMetaDataEntry<L>> silentWriteCache;
	private final CacheProperties properties;

	public InfinispanSessionMetaDataFactory(EmbeddedCacheConfiguration configuration) {
		this.cache = configuration.getCache();
		this.readForUpdateCache = configuration.getReadForUpdateCache();
		this.writeOnlyCache = configuration.getWriteOnlyCache();
		this.silentWriteCache = configuration.getSilentWriteCache();
		this.properties = configuration.getCacheProperties();
	}

	@Override
	public CompletionStage<ContextualSessionMetaDataEntry<L>> createValueAsync(String id, Duration defaultTimeout) {
		DefaultSessionMetaDataEntry<L> entry = new DefaultSessionMetaDataEntry<>();
		entry.setTimeout(defaultTimeout);
		return this.writeOnlyCache.putAsync(new SessionMetaDataKey(id), entry).thenApply(v -> entry);
	}

	@Override
	public CompletionStage<ContextualSessionMetaDataEntry<L>> findValueAsync(String id) {
		return this.getValueAsync(id, EnumSet.noneOf(Flag.class));
	}

	@Override
	public CompletionStage<ContextualSessionMetaDataEntry<L>> tryValueAsync(String id) {
		return this.getValueAsync(id, TRY_LOCK_FLAGS);
	}

	private CompletionStage<ContextualSessionMetaDataEntry<L>> getValueAsync(String id, Set<Flag> flags) {
		return this.readForUpdateCache.getAdvancedCache().withFlags(flags).getAsync(new SessionMetaDataKey(id));
	}

	@Override
	public CompletionStage<Void> removeAsync(String id) {
		return this.deleteAsync(this.writeOnlyCache, id);
	}

	@Override
	public CompletionStage<Void> purgeAsync(String id) {
		return this.deleteAsync(this.silentWriteCache, id);
	}

	private CompletionStage<Void> deleteAsync(Cache<SessionMetaDataKey, ContextualSessionMetaDataEntry<L>> cache, String id) {
		return cache.removeAsync(new SessionMetaDataKey(id)).thenAccept(Functions.discardingConsumer());
	}

	@Override
	public ImmutableSessionMetaData createImmutableSessionMetaData(String id, ContextualSessionMetaDataEntry<L> entry) {
		return new DefaultImmutableSessionMetaData(entry);
	}

	@Override
	public InvalidatableSessionMetaData createSessionMetaData(String id, ContextualSessionMetaDataEntry<L> entry) {
		MutableSessionMetaDataOffsetValues delta = this.properties.isTransactional() && entry.isNew() ? null : MutableSessionMetaDataOffsetValues.from(entry);
		CacheEntryMutator mutator = (delta != null) ? new EmbeddedCacheEntryComputeMutator<>(this.cache, new SessionMetaDataKey(id), new SessionMetaDataEntryFunction<>(delta)) : CacheEntryMutator.NO_OP;
		return new DefaultSessionMetaData((delta != null) ? new MutableSessionMetaDataEntry(entry, delta) : entry, mutator);
	}

	@Override
	public void close() {
	}
}
