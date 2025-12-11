/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

import javax.management.ObjectName;

import jakarta.transaction.TransactionManager;

import io.netty.channel.Channel;

import org.infinispan.api.async.AsyncCacheEntryProcessor;
import org.infinispan.api.common.CacheEntry;
import org.infinispan.api.common.CacheEntryVersion;
import org.infinispan.api.common.CacheOptions;
import org.infinispan.api.common.CacheWriteOptions;
import org.infinispan.api.common.events.cache.CacheEntryEvent;
import org.infinispan.api.common.events.cache.CacheEntryEventType;
import org.infinispan.api.common.events.cache.CacheListenerOptions;
import org.infinispan.api.common.process.CacheEntryProcessorResult;
import org.infinispan.api.common.process.CacheProcessorOptions;
import org.infinispan.api.configuration.CacheConfiguration;
import org.infinispan.client.hotrod.CacheTopologyInfo;
import org.infinispan.client.hotrod.DataFormat;
import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.MetadataValue;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.client.hotrod.ServerStatistics;
import org.infinispan.client.hotrod.StreamingRemoteCache;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.RemoteCacheConfiguration;
import org.infinispan.client.hotrod.event.impl.ClientListenerNotifier;
import org.infinispan.client.hotrod.impl.ClientStatistics;
import org.infinispan.client.hotrod.impl.InternalRemoteCache;
import org.infinispan.client.hotrod.impl.operations.CacheOperationsFactory;
import org.infinispan.client.hotrod.impl.operations.GetWithMetadataOperation.GetWithMetadataResult;
import org.infinispan.client.hotrod.impl.operations.PingResponse;
import org.infinispan.client.hotrod.impl.query.RemoteQueryFactory;
import org.infinispan.client.hotrod.impl.transport.netty.OperationDispatcher;
import org.infinispan.commons.api.query.ContinuousQuery;
import org.infinispan.commons.api.query.Query;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.commons.util.CloseableIterator;
import org.infinispan.commons.util.CloseableIteratorCollection;
import org.infinispan.commons.util.CloseableIteratorSet;
import org.infinispan.commons.util.IntSet;
import org.reactivestreams.Publisher;
import org.wildfly.clustering.cache.infinispan.BlockingBasicCacheDecorator;

/**
 * An abstract delegating remote cache implementation.
 * N.B. Implements {@link InternalRemoteCache} to support casting, as required by {@link org.infinispan.client.hotrod.Search#getQueryFactory(RemoteCache)}.
 * @param <K> the cache key type
 * @param <V> the cache value type
 * @author Paul Ferraro
 */
public class RemoteCacheDecorator<K, V> extends BlockingBasicCacheDecorator<K, V> implements InternalRemoteCache<K, V> {
	private static final Duration DEFAULT_LIFESPAN = Duration.ZERO;
	private static final Duration DEFAULT_MAX_IDLE = Duration.ZERO;

	private final InternalRemoteCache<K, V> cache;
	private final UnaryOperator<InternalRemoteCache<K, V>> decorator;

	/**
	 * Creates a remote cache decorator.
	 * @param cache the decorated remote cache
	 * @param decorator the cache decorator.
	 */
	protected RemoteCacheDecorator(InternalRemoteCache<K, V> cache, UnaryOperator<InternalRemoteCache<K, V>> decorator) {
		super(cache, Duration.ofMillis(Math.max(cache.getRemoteCacheContainer().getConfiguration().socketTimeout(), cache.getRemoteCacheContainer().getConfiguration().transactionTimeout())));
		this.cache = cache;
		this.decorator = decorator;
	}

	@SuppressWarnings("removal")
	@Override
	public <T> Query<T> query(String query) {
		return this.createQueryFactory().create(query);
	}

	@SuppressWarnings("removal")
	@Override
	public ContinuousQuery<K, V> continuousQuery() {
		return this.createQueryFactory().continuousQuery(this);
	}

	@SuppressWarnings("removal")
	private RemoteQueryFactory createQueryFactory() {
		RemoteCacheContainer container = this.getRemoteCacheContainer();
		// Prefer RemoteCache marshaller
		Marshaller marshaller = Optional.ofNullable(container.getConfiguration().remoteCaches().get(this.getName())).map(RemoteCacheConfiguration::marshaller).orElse(container.getMarshaller());
		return new RemoteQueryFactory(new RemoteCacheDecorator<>(this.cache, this.decorator) {
			@Override
			public RemoteCacheContainer getRemoteCacheContainer() {
				return new RemoteCacheContainerDecorator(container) {
					@Override
					public Marshaller getMarshaller() {
						return marshaller;
					}
				};
			}
		});
	}

	@Override
	protected <T> T join(CompletableFuture<T> future) {
		return this.cache.getDispatcher().await(future);
	}

	@Override
	public CompletableFuture<V> putAsync(K key, V value) {
		return this.putAsync(key, value, DEFAULT_LIFESPAN.getSeconds(), TimeUnit.SECONDS);
	}

	@Override
	public CompletableFuture<V> putAsync(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		return this.putAsync(key, value, lifespan, lifespanUnit, DEFAULT_MAX_IDLE.getSeconds(), TimeUnit.SECONDS);
	}

	@Override
	public CompletableFuture<V> putAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		return this.cache.putAsync(key, value, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
	}

	@Override
	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> data) {
		return this.putAllAsync(data, DEFAULT_LIFESPAN.getSeconds(), TimeUnit.SECONDS);
	}

	@Override
	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> data, long lifespan, TimeUnit lifespanUnit) {
		return this.putAllAsync(data, lifespan, lifespanUnit, DEFAULT_MAX_IDLE.getSeconds(), TimeUnit.SECONDS);
	}

	@Override
	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> data, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		return this.cache.putAllAsync(data, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
	}

	@Override
	public CompletableFuture<V> putIfAbsentAsync(K key, V value) {
		return this.putIfAbsentAsync(key, value, DEFAULT_LIFESPAN.getSeconds(), TimeUnit.SECONDS);
	}

	@Override
	public CompletableFuture<V> putIfAbsentAsync(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		return this.putIfAbsentAsync(key, value, lifespan, lifespanUnit, DEFAULT_MAX_IDLE.getSeconds(), TimeUnit.SECONDS);
	}

	@Override
	public CompletableFuture<V> putIfAbsentAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		return this.cache.putIfAbsentAsync(key, value, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
	}

	@Override
	public CompletableFuture<V> replaceAsync(K key, V value) {
		return this.replaceAsync(key, value, DEFAULT_LIFESPAN.getSeconds(), TimeUnit.SECONDS);
	}

	@Override
	public CompletableFuture<V> replaceAsync(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		return this.replaceAsync(key, value, lifespan, lifespanUnit, DEFAULT_MAX_IDLE.getSeconds(), TimeUnit.SECONDS);
	}

	@Override
	public CompletableFuture<V> replaceAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		return this.cache.replaceAsync(key, value, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
	}

	@Override
	public CompletableFuture<Boolean> replaceAsync(K key, V oldValue, V newValue) {
		return this.replaceAsync(key, oldValue, newValue, DEFAULT_LIFESPAN.getSeconds(), TimeUnit.SECONDS);
	}

	@Override
	public CompletableFuture<Boolean> replaceAsync(K key, V oldValue, V newValue, long lifespan, TimeUnit lifespanUnit) {
		return this.replaceAsync(key, oldValue, newValue, lifespan, lifespanUnit, DEFAULT_MAX_IDLE.getSeconds(), TimeUnit.SECONDS);
	}

	@Override
	public CompletableFuture<Boolean> replaceAsync(K key, V oldValue, V newValue, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		return this.cache.replaceAsync(key, oldValue, newValue, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
	}

	@Override
	public CacheTopologyInfo getCacheTopologyInfo() {
		return this.cache.getCacheTopologyInfo();
	}

	@Override
	public String getProtocolVersion() {
		return this.cache.getProtocolVersion();
	}

	@Override
	public RemoteCacheContainer getRemoteCacheContainer() {
		return this.cache.getRemoteCacheContainer();
	}

	@Deprecated
	@Override
	public org.infinispan.client.hotrod.RemoteCacheManager getRemoteCacheManager() {
		return this.cache.getRemoteCacheManager();
	}

	@Override
	public TransactionManager getTransactionManager() {
		return this.cache.getTransactionManager();
	}

	@Override
	public ClientStatistics clientStatistics() {
		return this.cache.clientStatistics();
	}

	@Override
	public ServerStatistics serverStatistics() {
		return this.cache.serverStatistics();
	}

	@Override
	public CompletionStage<ServerStatistics> serverStatisticsAsync() {
		return this.cache.serverStatisticsAsync();
	}

	@Override
	public StreamingRemoteCache<K> streaming() {
		return this.cache.streaming();
	}

	@Override
	public DataFormat getDataFormat() {
		return this.cache.getDataFormat();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, U> InternalRemoteCache<T, U> withDataFormat(DataFormat dataFormat) {
		return (InternalRemoteCache<T, U>) this.decorator.apply(this.cache.withDataFormat(dataFormat));
	}

	@Override
	public Set<Flag> flags() {
		return this.cache.flags();
	}

	@Override
	public InternalRemoteCache<K, V> withFlags(Flag... flags) {
		return this.decorator.apply(this.cache.withFlags(flags));
	}

	@Override
	public InternalRemoteCache<K, V> noFlags() {
		return this.decorator.apply(this.cache.noFlags());
	}

	// Listenable methods

	@Override
	public void addClientListener(Object listener) {
		this.cache.addClientListener(listener);
	}

	@Override
	public void addClientListener(Object listener, Object[] filterFactoryParams, Object[] converterFactoryParams) {
		this.cache.addClientListener(listener, filterFactoryParams, converterFactoryParams);
	}

	@Override
	public void removeClientListener(Object listener) {
		this.cache.removeClientListener(listener);
	}

	@Override
	public <T> T execute(String taskName, Map<String, ?> params) {
		return this.cache.execute(taskName, params);
	}

	@Override
	public boolean isTransactional() {
		return this.cache.isTransactional();
	}

	@Override
	public CloseableIteratorSet<Map.Entry<K, V>> entrySet() {
		return this.cache.entrySet();
	}

	@Override
	public CloseableIteratorSet<K> keySet() {
		return this.cache.keySet();
	}

	@Override
	public CloseableIteratorCollection<V> values() {
		return this.cache.values();
	}

	@Override
	public CloseableIteratorSet<Map.Entry<K, V>> entrySet(IntSet segments) {
		return this.cache.entrySet(segments);
	}

	@Override
	public CloseableIteratorSet<K> keySet(IntSet segments) {
		return this.cache.keySet(segments);
	}

	@Override
	public CloseableIteratorCollection<V> values(IntSet segments) {
		return this.cache.values(segments);
	}

	@Override
	public Map<K, V> getAll(Set<? extends K> keys) {
		return this.cache.getAll(keys);
	}

	@Override
	public MetadataValue<V> getWithMetadata(K key) {
		return this.cache.getWithMetadata(key);
	}

	@Override
	public CompletableFuture<MetadataValue<V>> getWithMetadataAsync(K key) {
		return this.cache.getWithMetadataAsync(key);
	}

	@Override
	public boolean removeWithVersion(K key, long version) {
		return this.cache.removeWithVersion(key, version);
	}

	@Override
	public CompletableFuture<Boolean> removeWithVersionAsync(K key, long version) {
		return this.cache.removeWithVersionAsync(key, version);
	}

	@Override
	public boolean replaceWithVersion(K key, V newValue, long version) {
		return this.replaceWithVersion(key, newValue, version, (int) DEFAULT_LIFESPAN.getSeconds());
	}

	@Override
	public boolean replaceWithVersion(K key, V newValue, long version, int lifespanSeconds) {
		return this.replaceWithVersion(key, newValue, version, lifespanSeconds, (int) DEFAULT_MAX_IDLE.getSeconds());
	}

	@Override
	public boolean replaceWithVersion(K key, V newValue, long version, int lifespanSeconds, int maxIdleTimeSeconds) {
		return this.replaceWithVersion(key, newValue, version, lifespanSeconds, TimeUnit.SECONDS, maxIdleTimeSeconds, TimeUnit.SECONDS);
	}

	@Override
	public boolean replaceWithVersion(K key, V newValue, long version, long lifespanSeconds, TimeUnit lifespanTimeUnit, long maxIdle, TimeUnit maxIdleTimeUnit) {
		return this.cache.replaceWithVersion(key, newValue, version, lifespanSeconds, lifespanTimeUnit, maxIdle, maxIdleTimeUnit);
	}

	@Override
	public CompletableFuture<Boolean> replaceWithVersionAsync(K key, V newValue, long version) {
		return this.replaceWithVersionAsync(key, newValue, version, (int) DEFAULT_LIFESPAN.getSeconds());
	}

	@Override
	public CompletableFuture<Boolean> replaceWithVersionAsync(K key, V newValue, long version, int lifespanSeconds) {
		return this.replaceWithVersionAsync(key, newValue, version, lifespanSeconds, (int) DEFAULT_MAX_IDLE.getSeconds());
	}

	@Override
	public CompletableFuture<Boolean> replaceWithVersionAsync(K key, V newValue, long version, int lifespanSeconds, int maxIdleSeconds) {
		return this.replaceWithVersionAsync(key, newValue, version, lifespanSeconds, TimeUnit.SECONDS, maxIdleSeconds, TimeUnit.SECONDS);
	}

	@Override
	public CompletableFuture<Boolean> replaceWithVersionAsync(K key, V newValue, long version, long lifespanSeconds, TimeUnit lifespanTimeUnit, long maxIdle, TimeUnit maxIdleTimeUnit) {
		return this.cache.replaceWithVersionAsync(key, newValue, version, lifespanSeconds, lifespanTimeUnit, maxIdle, maxIdleTimeUnit);
	}

	// Bulk readers/writers

	@Override
	public CloseableIterator<Map.Entry<Object, Object>> retrieveEntries(String filterConverterFactory, Object[] filterConverterParams, Set<Integer> segments, int batchSize) {
		return this.cache.retrieveEntries(filterConverterFactory, filterConverterParams, segments, batchSize);
	}

	@Override
	public <E> Publisher<Map.Entry<K, E>> publishEntries(String filterConverterFactory, Object[] filterConverterParams, Set<Integer> segments, int batchSize) {
		return this.cache.publishEntries(filterConverterFactory, filterConverterParams, segments, batchSize);
	}

	@Override
	public CloseableIterator<Map.Entry<Object, Object>> retrieveEntriesByQuery(Query<?> filterQuery, Set<Integer> segments, int batchSize) {
		return this.cache.retrieveEntriesByQuery(filterQuery, segments, batchSize);
	}

	@Override
	public <E> Publisher<Map.Entry<K, E>> publishEntriesByQuery(Query<?> filterQuery, Set<Integer> segments, int batchSize) {
		return this.cache.publishEntriesByQuery(filterQuery, segments, batchSize);
	}

	@Override
	public CloseableIterator<Map.Entry<Object, MetadataValue<Object>>> retrieveEntriesWithMetadata(Set<Integer> segments, int batchSize) {
		return this.cache.retrieveEntriesWithMetadata(segments, batchSize);
	}

	@Override
	public Publisher<Map.Entry<K, MetadataValue<V>>> publishEntriesWithMetadata(Set<Integer> segments, int batchSize) {
		return this.cache.publishEntriesWithMetadata(segments, batchSize);
	}

	// Cache write operations

	// Internal methods

	@Override
	public byte[] getNameBytes() {
		return this.cache.getNameBytes();
	}

	@Override
	public CloseableIterator<K> keyIterator(IntSet segments) {
		return this.cache.keyIterator(segments);
	}

	@Override
	public CloseableIterator<Entry<K, V>> entryIterator(IntSet segments) {
		return this.cache.entryIterator(segments);
	}

	@Override
	public CompletionStage<GetWithMetadataResult<V>> getWithMetadataAsync(K key, Channel channel) {
		return this.cache.getWithMetadataAsync(key, channel);
	}

	@Override
	public int flagInt() {
		return this.cache.flagInt();
	}

	@Override
	public boolean hasForceReturnFlag() {
		return this.cache.hasForceReturnFlag();
	}

	@Override
	public void resolveStorage() {
		this.cache.resolveStorage();
	}

	@Override
	public void resolveStorage(MediaType key, MediaType value) {
		this.cache.resolveStorage(key, value);
	}

	@Override
	public void init(Configuration configuration, OperationDispatcher dispatcher) {
		this.cache.init(configuration, dispatcher);
	}

	@Override
	public void init(Configuration configuration, OperationDispatcher dispatcher, ObjectName jmxParent) {
		this.cache.init(configuration, dispatcher, jmxParent);
	}

	@Override
	public OperationDispatcher getDispatcher() {
		return this.cache.getDispatcher();
	}

	@Override
	public byte[] keyToBytes(Object o) {
		return this.cache.keyToBytes(o);
	}

	@Override
	public CompletionStage<PingResponse> ping() {
		return this.cache.ping();
	}

	@Override
	public Channel addNearCacheListener(Object listener, int bloomBits) {
		return this.cache.addNearCacheListener(listener, bloomBits);
	}

	@Override
	public CompletionStage<Void> updateBloomFilter() {
		return this.cache.updateBloomFilter();
	}

	@Override
	public CacheOperationsFactory getOperationsFactory() {
		return this.cache.getOperationsFactory();
	}

	@Override
	public ClientListenerNotifier getListenerNotifier() {
		return this.cache.getListenerNotifier();
	}

	@Override
	public CompletionStage<CacheConfiguration> configuration() {
		return this.cache.configuration();
	}

	@Override
	public CompletionStage<V> get(K key, CacheOptions options) {
		return this.cache.get(key, options);
	}

	@Override
	public CompletionStage<CacheEntry<K, V>> getEntry(K key, CacheOptions options) {
		return this.cache.getEntry(key, options);
	}

	@Override
	public CompletionStage<CacheEntry<K, V>> putIfAbsent(K key, V value, CacheWriteOptions options) {
		return this.cache.putIfAbsent(key, value, options);
	}

	@Override
	public CompletionStage<Boolean> setIfAbsent(K key, V value, CacheWriteOptions options) {
		return this.cache.setIfAbsent(key, value, options);
	}

	@Override
	public CompletionStage<CacheEntry<K, V>> put(K key, V value, CacheWriteOptions options) {
		return this.cache.put(key, value, options);
	}

	@Override
	public CompletionStage<Void> set(K key, V value, CacheWriteOptions options) {
		return this.cache.set(key, value, options);
	}

	@Override
	public CompletionStage<Boolean> replace(K key, V value, CacheEntryVersion version, CacheWriteOptions options) {
		return this.cache.replace(key, value, version, options);
	}

	@Override
	public CompletionStage<CacheEntry<K, V>> getOrReplaceEntry(K key, V value, CacheEntryVersion version, CacheWriteOptions options) {
		return this.cache.getOrReplaceEntry(key, value, version, options);
	}

	@Override
	public CompletionStage<Boolean> remove(K key, CacheOptions options) {
		return this.cache.remove(key, options);
	}

	@Override
	public CompletionStage<Boolean> remove(K key, CacheEntryVersion version, CacheOptions options) {
		return this.cache.remove(key, version, options);
	}

	@Override
	public CompletionStage<CacheEntry<K, V>> getAndRemove(K key, CacheOptions options) {
		return this.cache.getAndRemove(key, options);
	}

	@Override
	public java.util.concurrent.Flow.Publisher<K> keys(CacheOptions options) {
		return this.cache.keys(options);
	}

	@Override
	public java.util.concurrent.Flow.Publisher<CacheEntry<K, V>> entries(CacheOptions options) {
		return this.cache.entries(options);
	}

	@Override
	public CompletionStage<Void> putAll(Map<K, V> entries, CacheWriteOptions options) {
		return this.cache.putAll(entries, options);
	}

	@Override
	public CompletionStage<Void> putAll(java.util.concurrent.Flow.Publisher<CacheEntry<K, V>> entries, CacheWriteOptions options) {
		return this.cache.putAll(entries, options);
	}

	@Override
	public java.util.concurrent.Flow.Publisher<CacheEntry<K, V>> getAll(Set<K> keys, CacheOptions options) {
		return this.cache.getAll(keys, options);
	}

	@Override
	public java.util.concurrent.Flow.Publisher<CacheEntry<K, V>> getAll(CacheOptions options, K[] keys) {
		return this.cache.getAll(options, keys);
	}

	@Override
	public java.util.concurrent.Flow.Publisher<K> removeAll(Set<K> keys, CacheWriteOptions options) {
		return this.cache.removeAll(keys, options);
	}

	@Override
	public java.util.concurrent.Flow.Publisher<K> removeAll(java.util.concurrent.Flow.Publisher<K> keys, CacheWriteOptions options) {
		return this.cache.removeAll(keys, options);
	}

	@Override
	public java.util.concurrent.Flow.Publisher<CacheEntry<K, V>> getAndRemoveAll(Set<K> keys, CacheWriteOptions options) {
		return this.cache.getAndRemoveAll(keys, options);
	}

	@Override
	public java.util.concurrent.Flow.Publisher<CacheEntry<K, V>> getAndRemoveAll(java.util.concurrent.Flow.Publisher<K> keys, CacheWriteOptions options) {
		return this.cache.getAndRemoveAll(keys, options);
	}

	@Override
	public CompletionStage<Long> estimateSize(CacheOptions options) {
		return this.cache.estimateSize(options);
	}

	@Override
	public CompletionStage<Void> clear(CacheOptions options) {
		return this.cache.clear(options);
	}

	@Override
	public java.util.concurrent.Flow.Publisher<CacheEntryEvent<K, V>> listen(CacheListenerOptions options, CacheEntryEventType[] types) {
		return this.cache.listen(options, types);
	}

	@Override
	public <T> java.util.concurrent.Flow.Publisher<CacheEntryProcessorResult<K, T>> process(Set<K> keys, AsyncCacheEntryProcessor<K, V, T> task, CacheOptions options) {
		return this.cache.process(keys, task, options);
	}

	@Override
	public <T> java.util.concurrent.Flow.Publisher<CacheEntryProcessorResult<K, T>> processAll(AsyncCacheEntryProcessor<K, V, T> processor, CacheProcessorOptions options) {
		return this.cache.processAll(processor, options);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof RemoteCache cache)) return false;
		return this.getRemoteCacheContainer().equals(cache.getRemoteCacheContainer()) && super.equals(object);
	}
}
