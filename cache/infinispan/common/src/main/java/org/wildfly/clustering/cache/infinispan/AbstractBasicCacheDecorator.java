/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.infinispan.commons.api.BasicCache;
import org.infinispan.commons.api.query.ContinuousQuery;
import org.infinispan.commons.api.query.Query;

/**
 * Decorator of a {@link BasicCache}.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
public abstract class AbstractBasicCacheDecorator<K, V> implements BasicCache<K, V> {

	private final BasicCache<K, V> cache;

	/**
	 * Creates a cache decorator
	 * @param cache the decorated cache
	 */
	protected AbstractBasicCacheDecorator(BasicCache<K, V> cache) {
		this.cache = cache;
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
	public void start() {
		this.cache.start();
	}

	@Override
	public void stop() {
		this.cache.stop();
	}

	@Override
	public boolean containsValue(Object value) {
		return this.cache.containsValue(value);
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return this.cache.entrySet();
	}

	@Override
	public boolean isEmpty() {
		return this.cache.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return this.cache.keySet();
	}

	@Override
	public Collection<V> values() {
		return this.cache.values();
	}

	@Override
	public CompletableFuture<Void> clearAsync() {
		return this.cache.clearAsync();
	}

	@Override
	public CompletableFuture<Boolean> containsKeyAsync(K key) {
		return this.cache.containsKeyAsync(key);
	}

	@Override
	public CompletableFuture<V> computeAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return this.cache.computeAsync(key, remappingFunction);
	}

	@Override
	public CompletableFuture<V> computeAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.cache.computeAsync(key, remappingFunction, lifespan, lifespanUnit);
	}

	@Override
	public CompletableFuture<V> computeAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.cache.computeAsync(key, remappingFunction, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit);
	}

	@Override
	public CompletableFuture<V> computeIfAbsentAsync(K key, Function<? super K, ? extends V> mappingFunction) {
		return this.cache.computeIfAbsentAsync(key, mappingFunction);
	}

	@Override
	public CompletableFuture<V> computeIfAbsentAsync(K key, Function<? super K, ? extends V> mappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.cache.computeIfAbsentAsync(key, mappingFunction, lifespan, lifespanUnit);
	}

	@Override
	public CompletableFuture<V> computeIfAbsentAsync(K key, Function<? super K, ? extends V> mappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.cache.computeIfAbsentAsync(key, mappingFunction, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit);
	}

	@Override
	public CompletableFuture<V> computeIfPresentAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return this.cache.computeIfPresentAsync(key, remappingFunction);
	}

	@Override
	public CompletableFuture<V> computeIfPresentAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.cache.computeIfPresentAsync(key, remappingFunction, lifespan, lifespanUnit);
	}

	@Override
	public CompletableFuture<V> computeIfPresentAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.cache.computeIfPresentAsync(key, remappingFunction, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit);
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
	public CompletableFuture<V> mergeAsync(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return this.cache.mergeAsync(key, value, remappingFunction);
	}

	@Override
	public CompletableFuture<V> mergeAsync(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.cache.mergeAsync(key, value, remappingFunction, lifespan, lifespanUnit);
	}

	@Override
	public CompletableFuture<V> mergeAsync(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.cache.mergeAsync(key, value, remappingFunction, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit);
	}

	@Override
	public CompletableFuture<V> putAsync(K key, V value) {
		return this.cache.putAsync(key, value);
	}

	@Override
	public CompletableFuture<V> putAsync(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		return this.cache.putAsync(key, value, lifespan, lifespanUnit);
	}

	@Override
	public CompletableFuture<V> putAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		return this.cache.putAsync(key, value, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
	}

	@Override
	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> data) {
		return this.cache.putAllAsync(data);
	}

	@Override
	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> data, long lifespan, TimeUnit lifespanUnit) {
		return this.cache.putAllAsync(data, lifespan, lifespanUnit);
	}

	@Override
	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> data, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		return this.cache.putAllAsync(data, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
	}

	@Override
	public CompletableFuture<V> putIfAbsentAsync(K key, V value) {
		return this.cache.putIfAbsentAsync(key, value);
	}

	@Override
	public CompletableFuture<V> putIfAbsentAsync(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		return this.cache.putIfAbsentAsync(key, value, lifespan, lifespanUnit);
	}

	@Override
	public CompletableFuture<V> putIfAbsentAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		return this.cache.putIfAbsentAsync(key, value, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
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
	public CompletableFuture<V> replaceAsync(K key, V value) {
		return this.cache.replaceAsync(key, value);
	}

	@Override
	public CompletableFuture<V> replaceAsync(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		return this.cache.replaceAsync(key, value, lifespan, lifespanUnit);
	}

	@Override
	public CompletableFuture<V> replaceAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		return this.cache.replaceAsync(key, value, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
	}

	@Override
	public CompletableFuture<Boolean> replaceAsync(K key, V oldValue, V newValue) {
		return this.cache.replaceAsync(key, oldValue, newValue);
	}

	@Override
	public CompletableFuture<Boolean> replaceAsync(K key, V oldValue, V newValue, long lifespan, TimeUnit lifespanUnit) {
		return this.cache.replaceAsync(key, oldValue, newValue, lifespan, lifespanUnit);
	}

	@Override
	public CompletableFuture<Boolean> replaceAsync(K key, V oldValue, V newValue, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		return this.cache.replaceAsync(key, oldValue, newValue, lifespan, lifespanUnit, maxIdle, maxIdleUnit);
	}

	@Override
	public CompletableFuture<Long> sizeAsync() {
		return this.cache.sizeAsync();
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
	public boolean equals(Object object) {
		if (!(object instanceof BasicCache cache)) return false;
		return this.getName().equals(cache.getName());
	}

	@Override
	public int hashCode() {
		return this.cache.hashCode();
	}
}
