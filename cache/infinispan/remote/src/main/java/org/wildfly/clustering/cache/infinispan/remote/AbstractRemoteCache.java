/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.infinispan.client.hotrod.CacheTopologyInfo;
import org.infinispan.client.hotrod.DataFormat;
import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.MetadataValue;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.client.hotrod.ServerStatistics;
import org.infinispan.client.hotrod.StreamingRemoteCache;
import org.infinispan.client.hotrod.impl.RemoteCacheSupport;
import org.infinispan.client.hotrod.jmx.RemoteCacheClientStatisticsMXBean;
import org.infinispan.commons.api.query.ContinuousQuery;
import org.infinispan.commons.api.query.Query;
import org.infinispan.commons.util.CloseableIterator;
import org.infinispan.commons.util.CloseableIteratorCollection;
import org.infinispan.commons.util.CloseableIteratorSet;
import org.infinispan.commons.util.IntSet;
import org.reactivestreams.Publisher;

/**
 * An abstract delegating remote cache implementation.
 * @param <K> the cache key type
 * @param <V> the cache value type
 * @author Paul Ferraro
 */
public abstract class AbstractRemoteCache<K, V> extends RemoteCacheSupport<K, V> implements UnaryOperator<RemoteCache<K, V>> {
	private final RemoteCache<K, V> cache;

	protected AbstractRemoteCache(RemoteCache<K, V> cache) {
		this.cache = cache;
	}

	@Override
	public CompletableFuture<Boolean> replaceWithVersionAsync(K key, V newValue, long version, long lifespanSeconds, TimeUnit lifespanTimeUnit, long maxIdle, TimeUnit maxIdleTimeUnit) {
		return this.cache.replaceWithVersionAsync(key, newValue, version, lifespanSeconds, lifespanTimeUnit, maxIdle, maxIdleTimeUnit);
	}

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

	@Override
	public CloseableIteratorSet<K> keySet(IntSet segments) {
		return this.cache.keySet(segments);
	}

	@Override
	public CloseableIteratorCollection<V> values(IntSet segments) {
		return this.cache.values(segments);
	}

	@Override
	public CloseableIteratorSet<Map.Entry<K, V>> entrySet(IntSet segments) {
		return this.cache.entrySet(segments);
	}

	@Override
	public RemoteCacheClientStatisticsMXBean clientStatistics() {
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
	public RemoteCache<K, V> withFlags(Flag... flags) {
		return this.apply(this.cache.withFlags(flags));
	}

	@Override
	public RemoteCache<K, V> noFlags() {
		return this.apply(this.cache.noFlags());
	}

	@Override
	public Set<Flag> flags() {
		return this.cache.flags();
	}

	@Override
	public RemoteCacheContainer getRemoteCacheContainer() {
		return this.cache.getRemoteCacheContainer();
	}

	@Override
	public String getProtocolVersion() {
		return this.cache.getProtocolVersion();
	}

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
	public CacheTopologyInfo getCacheTopologyInfo() {
		return this.cache.getCacheTopologyInfo();
	}

	@Override
	public StreamingRemoteCache<K> streaming() {
		return this.cache.streaming();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, U> RemoteCache<T, U> withDataFormat(DataFormat dataFormat) {
		return (RemoteCache<T, U>) this.apply(this.cache.withDataFormat(dataFormat));
	}

	@Override
	public DataFormat getDataFormat() {
		return this.cache.getDataFormat();
	}

	@Override
	public boolean isTransactional() {
		return this.cache.isTransactional();
	}

	@Override
	public String getName() {
		return this.cache.getName();
	}

	@Override
	public String getVersion() {
		return this.cache.getVersion();
	}

	@Override
	public <T> Query<T> query(String query) {
		return this.cache.query(query);
	}

	@Override
	public ContinuousQuery<K, V> continuousQuery() {
		return this.cache.continuousQuery();
	}

	@Override
	public CompletableFuture<Void> clearAsync() {
		return this.cache.clearAsync();
	}

	@Override
	public boolean isEmpty() {
		return this.cache.isEmpty();
	}

	@Override
	public boolean containsValue(Object value) {
		return this.cache.containsValue(value);
	}

	@Override
	public void start() {
		this.cache.start();
	}

	@Override
	public void stop() {
		this.cache.stop();
	}

	@Override
	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> data, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		return this.cache.putAllAsync(data, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
	}

	@Override
	public CompletableFuture<V> putIfAbsentAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		return this.cache.putIfAbsentAsync(key, value, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
	}

	@Override
	public CompletableFuture<Boolean> replaceAsync(K key, V oldValue, V newValue, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		return this.cache.replaceAsync(key, oldValue, newValue, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
	}

	@Override
	public CompletableFuture<V> replaceAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		return this.cache.replaceAsync(key, value, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
	}

	@Override
	public CompletableFuture<V> getAsync(K key) {
		return this.cache.getAsync(key);
	}

	@Override
	public CompletableFuture<Map<K, V>> getAllAsync(Set<?> keys) {
		return this.cache.getAllAsync(keys);
	}

	@Override
	public CompletableFuture<MetadataValue<V>> getWithMetadataAsync(K key) {
		return this.cache.getWithMetadataAsync(key);
	}

	@Override
	public CompletableFuture<Boolean> containsKeyAsync(K key) {
		return this.cache.containsKeyAsync(key);
	}

	@Override
	public CompletableFuture<V> putAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		return this.cache.putAsync(key, value, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
	}

	@Override
	public CompletableFuture<V> removeAsync(Object key) {
		return this.cache.removeAsync(key);
	}

	@Override
	public CompletableFuture<Boolean> removeAsync(Object key, Object value) {
		return this.cache.removeAsync(key, value);
	}

	@Override
	public CompletableFuture<Boolean> removeWithVersionAsync(K key, long version) {
		return this.cache.removeWithVersionAsync(key, version);
	}

	@Override
	public CompletableFuture<V> mergeAsync(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.cache.mergeAsync(key, value, remappingFunction, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit);
	}

	@Override
	public CompletableFuture<V> computeAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		return this.cache.computeAsync(key, remappingFunction, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
	}

	@Override
	public CompletableFuture<V> computeIfAbsentAsync(K key, Function<? super K, ? extends V> mappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		return this.cache.computeIfAbsentAsync(key, mappingFunction, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
	}

	@Override
	public CompletableFuture<V> computeIfPresentAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		return this.cache.computeIfPresentAsync(key, remappingFunction, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		this.cache.replaceAll(function);
	}

	@Override
	public CompletableFuture<Long> sizeAsync() {
		return this.cache.sizeAsync();
	}
}
