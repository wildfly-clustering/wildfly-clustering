/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import jakarta.transaction.TransactionManager;

import org.infinispan.AdvancedCache;
import org.infinispan.CacheCollection;
import org.infinispan.CachePublisher;
import org.infinispan.CacheSet;
import org.infinispan.LockedStream;
import org.infinispan.batch.BatchContainer;
import org.infinispan.cache.impl.InternalCache;
import org.infinispan.commons.api.query.ContinuousQuery;
import org.infinispan.commons.api.query.Query;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.Configurations;
import org.infinispan.container.DataContainer;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.context.Flag;
import org.infinispan.distribution.DistributionManager;
import org.infinispan.encoding.DataConversion;
import org.infinispan.expiration.ExpirationManager;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.metadata.EmbeddedMetadata;
import org.infinispan.metadata.Metadata;
import org.infinispan.notifications.cachelistener.filter.CacheEventConverter;
import org.infinispan.notifications.cachelistener.filter.CacheEventFilter;
import org.infinispan.partitionhandling.AvailabilityMode;
import org.infinispan.remoting.rpc.RpcManager;
import org.infinispan.security.AuthorizationManager;
import org.infinispan.stats.Stats;
import org.infinispan.util.concurrent.locks.LockManager;
import org.wildfly.clustering.cache.infinispan.NonBlockingBasicCacheDecorator;

/**
 * An {@link AdvancedCache} decorator.
 * N.B. Implements InternalCache which is required by {@link ComponentRegistry#of(org.infinispan.Cache)}.
 * @author Paul Ferraro
 * @param <K> the cache key type
 * @param <V> the cache value type
 */
public class AdvancedCacheDecorator<K, V> extends NonBlockingBasicCacheDecorator<K, V> implements AdvancedCache<K, V>, InternalCache<K, V> {
	private final AdvancedCache<K, V> cache;
	private final InternalCache<K, V> internalCache;
	private final UnaryOperator<AdvancedCache<K, V>> decorator;
	private final Metadata defaultMetadata;

	/**
	 * Creates an embedded cache decorator.
	 * @param cache the cache to which to delegate.
	 * @param decorator the cache decorator
	 */
	@SuppressWarnings("unchecked")
	protected AdvancedCacheDecorator(AdvancedCache<K, V> cache, UnaryOperator<AdvancedCache<K, V>> decorator) {
		super(cache);
		this.cache = cache;
		this.internalCache = (InternalCache<K, V>) cache;
		this.decorator = decorator;
		this.defaultMetadata = Configurations.newDefaultMetadata(cache.getCacheConfiguration());
	}

	@Override
	public ComponentRegistry getComponentRegistry() {
		return this.internalCache.getComponentRegistry();
	}

	@Override
	public boolean bypassInvocationContextFactory() {
		return this.internalCache.bypassInvocationContextFactory();
	}

	@Override
	public Configuration getCacheConfiguration() {
		return this.cache.getCacheConfiguration();
	}

	@Override
	public EmbeddedCacheManager getCacheManager() {
		return this.cache.getCacheManager();
	}

	@Override
	public AdvancedCache<K, V> getAdvancedCache() {
		return this;
	}

	// Component methods

	@Override
	public AvailabilityMode getAvailability() {
		return this.cache.getAvailability();
	}

	@Override
	public void setAvailability(AvailabilityMode availabilityMode) {
		this.cache.setAvailability(availabilityMode);
	}

	@Override
	public AuthorizationManager getAuthorizationManager() {
		return this.cache.getAuthorizationManager();
	}

	@Override
	public BatchContainer getBatchContainer() {
		return this.cache.getBatchContainer();
	}

	@Override
	public ClassLoader getClassLoader() {
		return this.cache.getClassLoader();
	}

	@Override
	public ComponentStatus getStatus() {
		return this.cache.getStatus();
	}

	@Override
	public DataContainer<K, V> getDataContainer() {
		return this.cache.getDataContainer();
	}

	@Override
	public DataConversion getKeyDataConversion() {
		return this.cache.getKeyDataConversion();
	}

	@Override
	public DataConversion getValueDataConversion() {
		return this.cache.getValueDataConversion();
	}

	@Override
	public DistributionManager getDistributionManager() {
		return this.cache.getDistributionManager();
	}

	@Override
	public ExpirationManager<K, V> getExpirationManager() {
		return this.cache.getExpirationManager();
	}

	@Override
	public LockManager getLockManager() {
		return this.cache.getLockManager();
	}

	@Override
	public RpcManager getRpcManager() {
		return this.cache.getRpcManager();
	}

	@Override
	public Stats getStats() {
		return this.cache.getStats();
	}

	@Override
	public TransactionManager getTransactionManager() {
		return this.cache.getTransactionManager();
	}

	@Override
	public XAResource getXAResource() {
		return this.cache.getXAResource();
	}

	// Wrapping methods

	@Override
	public AdvancedCache<K, V> lockAs(Object lockOwner) {
		return this.decorator.apply(this.cache.lockAs(lockOwner));
	}

	@Override
	public AdvancedCache<K, V> noFlags() {
		return this.decorator.apply(this.cache.noFlags());
	}

	@Override
	public AdvancedCache<K, V> transform(Function<AdvancedCache<K, V>, ? extends AdvancedCache<K, V>> transformation) {
		return this.decorator.apply(this.cache.transform(transformation));
	}

	@Override
	public AdvancedCache<K, V> withFlags(Flag flag) {
		return this.decorator.apply(this.cache.withFlags(flag));
	}

	@Override
	public AdvancedCache<K, V> withFlags(Flag... flags) {
		return this.decorator.apply(this.cache.withFlags(flags));
	}

	@Override
	public AdvancedCache<K, V> withFlags(Collection<Flag> flags) {
		return this.decorator.apply(this.cache.withFlags(flags));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K1, V1> AdvancedCache<K1, V1> withMediaType(MediaType keyMediaType, MediaType valueMediaType) {
		return (AdvancedCache<K1, V1>) this.decorator.apply(this.cache.withMediaType(keyMediaType, valueMediaType));
	}

	@Override
	public AdvancedCache<K, V> withStorageMediaType() {
		return this.decorator.apply(this.cache.withStorageMediaType());
	}

	@Override
	public AdvancedCache<K, V> withSubject(Subject subject) {
		return this.decorator.apply(this.cache.withSubject(subject));
	}

	// Read/Write operations

	@Override
	public CachePublisher<K, V> cachePublisher() {
		return this.cache.cachePublisher();
	}

	@Override
	public CacheSet<CacheEntry<K, V>> cacheEntrySet() {
		return this.cache.cacheEntrySet();
	}

	@Override
	public CacheSet<Map.Entry<K, V>> entrySet() {
		return this.cache.entrySet();
	}

	@Override
	public CacheSet<K> keySet() {
		return this.cache.keySet();
	}

	@Override
	public LockedStream<K, V> lockedStream() {
		return this.cache.lockedStream();
	}

	@Override
	public CacheCollection<V> values() {
		return this.cache.values();
	}

	@Override
	public void clear() {
		this.cache.clear();
	}

	@Override
	public CompletableFuture<Void> clearAsync() {
		return this.cache.clearAsync();
	}

	@Override
	public boolean containsKey(Object key) {
		return this.cache.containsKey(key);
	}

	@Override
	public CompletableFuture<Boolean> containsKeyAsync(K key) {
		return this.cache.containsKeyAsync(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return this.cache.containsValue(value);
	}

	@Override
	public int size() {
		return this.cache.size();
	}

	@Override
	public CompletableFuture<Long> sizeAsync() {
		return this.cache.sizeAsync();
	}

	@Override
	public V getOrDefault(Object key, V defaultValue) {
		return this.cache.getOrDefault(key, defaultValue);
	}

	@Override
	public CompletableFuture<Map<K, V>> getAllAsync(Set<?> keys) {
		return this.cache.getAllAsync(keys);
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return this.compute(key, remappingFunction, this.defaultMetadata);
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.compute(key, remappingFunction, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(this.defaultMetadata.maxIdle()).build());
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.compute(key, remappingFunction, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(maxIdleTime, maxIdleTimeUnit).build());
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, Metadata metadata) {
		return this.cache.compute(key, remappingFunction, metadata);
	}

	@Override
	public CompletableFuture<V> computeAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return this.computeAsync(key, remappingFunction, this.defaultMetadata);
	}

	@Override
	public CompletableFuture<V> computeAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.computeAsync(key, remappingFunction, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(this.defaultMetadata.maxIdle()).build());
	}

	@Override
	public CompletableFuture<V> computeAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.computeAsync(key, remappingFunction, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(maxIdleTime, maxIdleTimeUnit).build());
	}

	@Override
	public CompletableFuture<V> computeAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, Metadata metadata) {
		return this.cache.computeAsync(key, remappingFunction, metadata);
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return this.computeIfAbsent(key, mappingFunction, this.defaultMetadata);
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.computeIfAbsent(key, mappingFunction, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(this.defaultMetadata.maxIdle()).build());
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.computeIfAbsent(key, mappingFunction, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(maxIdleTime, maxIdleTimeUnit).build());
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, Metadata metadata) {
		return this.cache.computeIfAbsent(key, mappingFunction, metadata);
	}

	@Override
	public CompletableFuture<V> computeIfAbsentAsync(K key, Function<? super K, ? extends V> mappingFunction) {
		return this.computeIfAbsentAsync(key, mappingFunction, this.defaultMetadata);
	}

	@Override
	public CompletableFuture<V> computeIfAbsentAsync(K key, Function<? super K, ? extends V> mappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.computeIfAbsentAsync(key, mappingFunction, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(this.defaultMetadata.maxIdle()).build());
	}

	@Override
	public CompletableFuture<V> computeIfAbsentAsync(K key, Function<? super K, ? extends V> mappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.computeIfAbsentAsync(key, mappingFunction, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(maxIdleTime, maxIdleTimeUnit).build());
	}

	@Override
	public CompletableFuture<V> computeIfAbsentAsync(K key, Function<? super K, ? extends V> mappingFunction, Metadata metadata) {
		return this.cache.computeIfAbsentAsync(key, mappingFunction, metadata);
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return this.computeIfPresent(key, remappingFunction, this.defaultMetadata);
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.computeIfPresent(key, remappingFunction, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(this.defaultMetadata.maxIdle()).build());
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.computeIfPresent(key, remappingFunction, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(maxIdleTime, maxIdleTimeUnit).build());
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, Metadata metadata) {
		return this.cache.computeIfPresent(key, remappingFunction, metadata);
	}

	@Override
	public CompletableFuture<V> computeIfPresentAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return this.computeIfPresentAsync(key, remappingFunction, this.defaultMetadata);
	}

	@Override
	public CompletableFuture<V> computeIfPresentAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.computeIfPresentAsync(key, remappingFunction, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(this.defaultMetadata.maxIdle()).build());
	}

	@Override
	public CompletableFuture<V> computeIfPresentAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.computeIfPresentAsync(key, remappingFunction, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(maxIdleTime, maxIdleTimeUnit).build());
	}

	@Override
	public CompletableFuture<V> computeIfPresentAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, Metadata metadata) {
		return this.cache.computeIfPresentAsync(key, remappingFunction, metadata);
	}

	@Override
	public Map<K, V> getAll(Set<?> keys) {
		return this.cache.getAll(keys);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean lock(K... keys) {
		return this.lock(Set.of(keys));
	}

	@Override
	public boolean lock(Collection<? extends K> keys) {
		return this.cache.lock(keys);
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return this.merge(key, value, remappingFunction, this.defaultMetadata);
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.merge(key, value, remappingFunction, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(this.defaultMetadata.maxIdle()).build());
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.merge(key, value, remappingFunction, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(maxIdleTime, maxIdleTimeUnit).build());
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, Metadata metadata) {
		return this.cache.merge(key, value, remappingFunction, metadata);
	}

	@Override
	public CompletableFuture<V> mergeAsync(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return this.mergeAsync(key, value, remappingFunction, this.defaultMetadata);
	}

	@Override
	public CompletableFuture<V> mergeAsync(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.mergeAsync(key, value, remappingFunction, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(this.defaultMetadata.maxIdle()).build());
	}

	@Override
	public CompletableFuture<V> mergeAsync(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.mergeAsync(key, value, remappingFunction, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(maxIdleTime, maxIdleTimeUnit).build());
	}

	@Override
	public CompletableFuture<V> mergeAsync(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, Metadata metadata) {
		return this.cache.mergeAsync(key, value, remappingFunction, metadata);
	}

	@Override
	public V put(K key, V value) {
		return this.put(key, value, this.defaultMetadata);
	}

	@Override
	public V put(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		return this.put(key, value, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(this.defaultMetadata.maxIdle()).build());
	}

	@Override
	public V put(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.put(key, value, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(maxIdleTime, maxIdleTimeUnit).build());
	}

	@Override
	public V put(K key, V value, Metadata metadata) {
		return this.cache.put(key, value, metadata);
	}

	@Override
	public CompletableFuture<V> putAsync(K key, V value) {
		return this.putAsync(key, value, this.defaultMetadata);
	}

	@Override
	public CompletableFuture<V> putAsync(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		return this.putAsync(key, value, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(this.defaultMetadata.maxIdle()).build());
	}

	@Override
	public CompletableFuture<V> putAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.putAsync(key, value, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(maxIdleTime, maxIdleTimeUnit).build());
	}

	@Override
	public CompletableFuture<V> putAsync(K key, V value, Metadata metadata) {
		return this.cache.putAsync(key, value, metadata);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		this.putAll(map, this.defaultMetadata);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map, long lifespan, TimeUnit lifespanUnit) {
		this.putAll(map, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(this.defaultMetadata.maxIdle()).build());
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		this.putAll(map, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(maxIdleTime, maxIdleTimeUnit).build());
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map, Metadata metadata) {
		this.cache.putAll(map, metadata);
	}

	@Override
	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> data) {
		return this.putAllAsync(data, this.defaultMetadata);
	}

	@Override
	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> data, long lifespan, TimeUnit lifespanUnit) {
		return this.putAllAsync(data, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(this.defaultMetadata.maxIdle()).build());
	}

	@Override
	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> data, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.putAllAsync(data, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(maxIdleTime, maxIdleTimeUnit).build());
	}

	@Override
	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> map, Metadata metadata) {
		return this.cache.putAllAsync(map, metadata);
	}

	@Override
	public void putForExternalRead(K key, V value) {
		this.putForExternalRead(key, value, this.defaultMetadata);
	}

	@Override
	public void putForExternalRead(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		this.putForExternalRead(key, value, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(this.defaultMetadata.maxIdle()).build());
	}

	@Override
	public void putForExternalRead(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		this.putForExternalRead(key, value, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(maxIdleTime, maxIdleTimeUnit).build());
	}

	@Override
	public void putForExternalRead(K key, V value, Metadata metadata) {
		this.cache.putForExternalRead(key, value, metadata);
	}

	@Override
	public V putIfAbsent(K key, V value) {
		return this.putIfAbsent(key, value, this.defaultMetadata);
	}

	@Override
	public V putIfAbsent(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		return this.putIfAbsent(key, value, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(this.defaultMetadata.maxIdle()).build());
	}

	@Override
	public V putIfAbsent(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.putIfAbsent(key, value, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(maxIdleTime, maxIdleTimeUnit).build());
	}

	@Override
	public V putIfAbsent(K key, V value, Metadata metadata) {
		return this.cache.putIfAbsent(key, value, metadata);
	}

	@Override
	public CompletableFuture<V> putIfAbsentAsync(K key, V value) {
		return this.putIfAbsentAsync(key, value, this.defaultMetadata);
	}

	@Override
	public CompletableFuture<V> putIfAbsentAsync(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		return this.putIfAbsentAsync(key, value, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(this.defaultMetadata.maxIdle()).build());
	}

	@Override
	public CompletableFuture<V> putIfAbsentAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.putIfAbsentAsync(key, value, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(maxIdleTime, maxIdleTimeUnit).build());
	}

	@Override
	public CompletableFuture<V> putIfAbsentAsync(K key, V value, Metadata metadata) {
		return this.cache.putIfAbsentAsync(key, value, metadata);
	}

	@Override
	public V replace(K key, V value) {
		return this.replace(key, value, this.defaultMetadata);
	}

	@Override
	public V replace(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		return this.replace(key, value, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(this.defaultMetadata.maxIdle()).build());
	}

	@Override
	public V replace(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.replace(key, value, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(maxIdleTime, maxIdleTimeUnit).build());
	}

	@Override
	public V replace(K key, V value, Metadata metadata) {
		return this.cache.replace(key, value, metadata);
	}

	@Override
	public CompletableFuture<V> replaceAsync(K key, V value) {
		return this.replaceAsync(key, value, this.defaultMetadata);
	}

	@Override
	public CompletableFuture<V> replaceAsync(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		return this.replaceAsync(key, value, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(this.defaultMetadata.maxIdle()).build());
	}

	@Override
	public CompletableFuture<V> replaceAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleUnit) {
		return this.replaceAsync(key, value, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(maxIdleTime, maxIdleUnit).build());
	}

	@Override
	public CompletableFuture<V> replaceAsync(K key, V value, Metadata metadata) {
		return this.cache.replaceAsync(key, value, metadata);
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		return this.replace(key, oldValue, newValue, this.defaultMetadata);
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue, long lifespan, TimeUnit lifespanUnit) {
		return this.replace(key, oldValue, newValue, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(this.defaultMetadata.maxIdle()).build());
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.replace(key, oldValue, newValue, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(maxIdleTime, maxIdleTimeUnit).build());
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue, Metadata metadata) {
		return this.cache.replace(key, oldValue, newValue, metadata);
	}

	@Override
	public CompletableFuture<Boolean> replaceAsync(K key, V oldValue, V newValue) {
		return this.replaceAsync(key, oldValue, newValue, this.defaultMetadata);
	}

	@Override
	public CompletableFuture<Boolean> replaceAsync(K key, V oldValue, V newValue, long lifespan, TimeUnit lifespanUnit) {
		return this.replaceAsync(key, oldValue, newValue, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(this.defaultMetadata.maxIdle()).build());
	}

	@Override
	public CompletableFuture<Boolean> replaceAsync(K key, V oldValue, V newValue, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.replaceAsync(key, oldValue, newValue, new EmbeddedMetadata.Builder().lifespan(lifespan, lifespanUnit).maxIdle(maxIdleTime, maxIdleTimeUnit).build());
	}

	@Override
	public CompletableFuture<Boolean> replaceAsync(K key, V oldValue, V newValue, Metadata metadata) {
		return this.cache.replaceAsync(key, oldValue, newValue, metadata);
	}

	// Mortality methods

	@Override
	public void evict(K key) {
		this.cache.evict(key);
	}

	@Override
	public CompletableFuture<Boolean> removeLifespanExpired(K key, V value, Long lifespan) {
		return this.cache.removeLifespanExpired(key, value, lifespan);
	}

	@Override
	public CompletableFuture<Boolean> removeMaxIdleExpired(K key, V value) {
		return this.cache.removeMaxIdleExpired(key, value);
	}

	@Override
	public CompletionStage<Boolean> touch(Object key, boolean touchEvenIfExpired) {
		return this.cache.touch(key, touchEvenIfExpired);
	}

	@Override
	public CompletionStage<Boolean> touch(Object key, int segment, boolean touchEvenIfExpired) {
		return this.cache.touch(key, segment, touchEvenIfExpired);
	}

	// CacheEntry methods

	@Override
	public CacheEntry<K, V> getCacheEntry(Object key) {
		return this.cache.getCacheEntry(key);
	}

	@Override
	public Map<K, CacheEntry<K, V>> getAllCacheEntries(Set<?> keys) {
		return this.cache.getAllCacheEntries(keys);
	}

	@Override
	public CompletableFuture<CacheEntry<K, V>> getCacheEntryAsync(Object key) {
		return this.cache.getCacheEntryAsync(key);
	}

	@Override
	public CompletableFuture<CacheEntry<K, V>> putAsyncEntry(K key, V value, Metadata metadata) {
		return this.cache.putAsyncEntry(key, value, metadata);
	}

	@Override
	public CompletableFuture<CacheEntry<K, V>> putIfAbsentAsyncEntry(K key, V value, Metadata metadata) {
		return this.cache.putIfAbsentAsyncEntry(key, value, metadata);
	}

	@Override
	public CompletableFuture<CacheEntry<K, V>> replaceAsyncEntry(K key, V value, Metadata metadata) {
		return this.cache.replaceAsyncEntry(key, value, metadata);
	}

	@Override
	public CompletableFuture<CacheEntry<K, V>> removeAsyncEntry(Object key) {
		return this.cache.removeAsyncEntry(key);
	}

	// Batching methods

	@Override
	public boolean startBatch() {
		return this.cache.startBatch();
	}

	@Override
	public void endBatch(boolean successful) {
		this.cache.endBatch(successful);
	}

	// Grouping methods

	@Override
	public Map<K, V> getGroup(String groupName) {
		return this.cache.getGroup(groupName);
	}

	@Override
	public void removeGroup(String groupName) {
		this.cache.removeGroup(groupName);
	}

	// Listenable methods

	@SuppressWarnings("removal")
	@Override
	public <C> CompletionStage<Void> addListenerAsync(Object listener, CacheEventFilter<? super K, ? super V> filter, CacheEventConverter<? super K, ? super V, C> converter) {
		AdvancedCache<K, V> cache = this.cache;
		return java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<>() {
			@Override
			public CompletionStage<Void> run() {
				return cache.addListenerAsync(listener, filter, converter);
			}
		});
	}

	@SuppressWarnings("removal")
	@Override
	public <C> CompletionStage<Void> addFilteredListenerAsync(Object listener, CacheEventFilter<? super K, ? super V> filter, CacheEventConverter<? super K, ? super V, C> converter, Set<Class<? extends Annotation>> filterAnnotations) {
		AdvancedCache<K, V> cache = this.cache;
		return java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<>() {
			@Override
			public CompletionStage<Void> run() {
				return cache.addFilteredListenerAsync(listener, filter, converter, filterAnnotations);
			}
		});
	}

	@SuppressWarnings("removal")
	@Override
	public <C> CompletionStage<Void> addStorageFormatFilteredListenerAsync(Object listener, CacheEventFilter<? super K, ? super V> filter, CacheEventConverter<? super K, ? super V, C> converter, Set<Class<? extends Annotation>> filterAnnotations) {
		AdvancedCache<K, V> cache = this.cache;
		return java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<>() {
			@Override
			public CompletionStage<Void> run() {
				return cache.addStorageFormatFilteredListenerAsync(listener, filter, converter, filterAnnotations);
			}
		});
	}

	@SuppressWarnings("removal")
	@Override
	public CompletionStage<Void> addListenerAsync(Object listener) {
		AdvancedCache<K, V> cache = this.cache;
		return java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<>() {
			@Override
			public CompletionStage<Void> run() {
				return cache.addListenerAsync(listener);
			}
		});
	}

	@Override
	public CompletionStage<Void> removeListenerAsync(Object listener) {
		return this.cache.removeListenerAsync(listener);
	}

	// Query methods

	@Override
	public <T> Query<T> query(String query) {
		return this.cache.query(query);
	}

	@Override
	public ContinuousQuery<K, V> continuousQuery() {
		return this.cache.continuousQuery();
	}
}
