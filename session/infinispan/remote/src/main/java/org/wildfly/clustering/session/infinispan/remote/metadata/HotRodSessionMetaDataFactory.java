/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.metadata;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheEntryComputeMutator;
import org.wildfly.clustering.server.offset.OffsetValue;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.metadata.InvalidatableSessionMetaData;
import org.wildfly.clustering.session.cache.metadata.SessionMetaDataFactory;
import org.wildfly.clustering.session.cache.metadata.fine.CompositeImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.metadata.fine.CompositeSessionMetaData;
import org.wildfly.clustering.session.cache.metadata.fine.DefaultSessionAccessMetaDataEntry;
import org.wildfly.clustering.session.cache.metadata.fine.DefaultSessionCreationMetaDataEntry;
import org.wildfly.clustering.session.cache.metadata.fine.DefaultSessionMetaDataEntry;
import org.wildfly.clustering.session.cache.metadata.fine.MutableSessionAccessMetaData;
import org.wildfly.clustering.session.cache.metadata.fine.MutableSessionAccessMetaDataOffsetValues;
import org.wildfly.clustering.session.cache.metadata.fine.MutableSessionCreationMetaData;
import org.wildfly.clustering.session.cache.metadata.fine.SessionAccessMetaData;
import org.wildfly.clustering.session.cache.metadata.fine.SessionAccessMetaDataEntry;
import org.wildfly.clustering.session.cache.metadata.fine.SessionAccessMetaDataEntryFunction;
import org.wildfly.clustering.session.cache.metadata.fine.SessionCreationMetaData;
import org.wildfly.clustering.session.cache.metadata.fine.SessionCreationMetaDataEntry;
import org.wildfly.clustering.session.cache.metadata.fine.SessionCreationMetaDataEntryFunction;
import org.wildfly.clustering.session.cache.metadata.fine.SessionMetaDataEntry;
import org.wildfly.common.function.Functions;

/**
 * Factory for creating {@link org.wildfly.clustering.session.SessionMetaData} backed by a pair of {@link RemoteCache} entries.
 * @author Paul Ferraro
 * @param <C> the local context type
 */
public class HotRodSessionMetaDataFactory<C> implements SessionMetaDataFactory<SessionMetaDataEntry<C>>, BiFunction<SessionCreationMetaDataEntry<C>, SessionAccessMetaDataEntry, SessionMetaDataEntry<C>> {

	private final RemoteCache<SessionCreationMetaDataKey, SessionCreationMetaDataEntry<C>> creationMetaDataCache;
	private final RemoteCache<SessionAccessMetaDataKey, SessionAccessMetaDataEntry> accessMetaDataCache;
	private final Flag[] ignoreReturnFlags;

	public HotRodSessionMetaDataFactory(RemoteCacheConfiguration configuration) {
		this.creationMetaDataCache = configuration.getCache();
		this.accessMetaDataCache = configuration.getCache();
		this.ignoreReturnFlags = configuration.getIgnoreReturnFlags();
	}

	@Override
	public CompletionStage<SessionMetaDataEntry<C>> createValueAsync(String id, Duration defaultTimeout) {
		SessionCreationMetaDataKey creationMetaDataKey = new SessionCreationMetaDataKey(id);
		SessionAccessMetaDataKey accessMetaDataKey = new SessionAccessMetaDataKey(id);
		SessionCreationMetaDataEntry<C> creationMetaData = new DefaultSessionCreationMetaDataEntry<>();
		creationMetaData.setTimeout(defaultTimeout);
		SessionAccessMetaDataEntry accessMetaData = new DefaultSessionAccessMetaDataEntry();
		CompletableFuture<?> creationStage = this.creationMetaDataCache.withFlags(this.ignoreReturnFlags).putAsync(creationMetaDataKey, creationMetaData);
		CompletableFuture<?> accessStage = this.accessMetaDataCache.withFlags(this.ignoreReturnFlags).putAsync(accessMetaDataKey, accessMetaData, 0L, TimeUnit.SECONDS, defaultTimeout.getSeconds(), TimeUnit.SECONDS);
		return CompletableFuture.allOf(creationStage, accessStage).thenApply(v -> new DefaultSessionMetaDataEntry<>(creationMetaData, accessMetaData));
	}

	@Override
	public CompletionStage<SessionMetaDataEntry<C>> findValueAsync(String id) {
		SessionCreationMetaDataKey creationMetaDataKey = new SessionCreationMetaDataKey(id);
		SessionAccessMetaDataKey accessMetaDataKey = new SessionAccessMetaDataKey(id);
		return this.creationMetaDataCache.getAsync(creationMetaDataKey).thenCombine(this.accessMetaDataCache.getAsync(accessMetaDataKey), this);
	}

	@Override
	public SessionMetaDataEntry<C> apply(SessionCreationMetaDataEntry<C> creationMetaData, SessionAccessMetaDataEntry accessMetaData) {
		return (creationMetaData != null) && (accessMetaData != null) ? new DefaultSessionMetaDataEntry<>(creationMetaData, accessMetaData) : null;
	}

	@Override
	public CompletionStage<Void> removeAsync(String id) {
		CompletableFuture<?> creationMetaData = this.creationMetaDataCache.withFlags(this.ignoreReturnFlags).removeAsync(new SessionCreationMetaDataKey(id));
		CompletableFuture<?> accessMetaData = this.accessMetaDataCache.withFlags(this.ignoreReturnFlags).removeAsync(new SessionAccessMetaDataKey(id));
		return CompletableFuture.allOf(creationMetaData, accessMetaData).thenAccept(Functions.discardingConsumer());
	}

	@Override
	public InvalidatableSessionMetaData createSessionMetaData(String id, SessionMetaDataEntry<C> entry) {
		OffsetValue<Duration> timeoutOffset = OffsetValue.from(entry.getCreationMetaDataEntry().getTimeout());
		SessionCreationMetaData creationMetaData = new MutableSessionCreationMetaData(entry.getCreationMetaDataEntry(), timeoutOffset);

		MutableSessionAccessMetaDataOffsetValues values = MutableSessionAccessMetaDataOffsetValues.from(entry.getAccessMetaDataEntry());
		SessionAccessMetaData accessMetaData = new MutableSessionAccessMetaData(entry.getAccessMetaDataEntry(), values);

		CacheEntryMutator creationMetaDataMutator = new RemoteCacheEntryComputeMutator<>(this.creationMetaDataCache, this.ignoreReturnFlags, new SessionCreationMetaDataKey(id), new SessionCreationMetaDataEntryFunction<>(timeoutOffset));
		CacheEntryMutator accessMetaDataMutator = new RemoteCacheEntryComputeMutator<>(this.accessMetaDataCache, this.ignoreReturnFlags, new SessionAccessMetaDataKey(id), new SessionAccessMetaDataEntryFunction(values), creationMetaData::getTimeout);
		CacheEntryMutator mutator = new CacheEntryMutator() {
			@Override
			public CompletionStage<Void> mutateAsync() {
				CompletionStage<Void> stage = !timeoutOffset.getOffset().isZero() ? creationMetaDataMutator.mutateAsync() : CompletableFuture.completedStage(null);
				return stage.thenAcceptBoth(accessMetaDataMutator.mutateAsync(), Functions.discardingBiConsumer());
			}
		};

		return new CompositeSessionMetaData(creationMetaData, accessMetaData, mutator);
	}

	@Override
	public ImmutableSessionMetaData createImmutableSessionMetaData(String id, SessionMetaDataEntry<C> entry) {
		return new CompositeImmutableSessionMetaData(entry.getCreationMetaDataEntry(), entry.getAccessMetaDataEntry());
	}

	@Override
	public void close() {
	}
}
