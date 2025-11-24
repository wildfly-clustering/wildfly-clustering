/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan;

import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.infinispan.commons.api.BasicCache;

/**
 * A {@link BasicCache} decorator for cache implementations whose synchronous methods always block.
 * Synchronous operations will delegating to asynchronous methods and wait for result.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
public class BlockingBasicCacheDecorator<K, V> extends AbstractBasicCacheDecorator<K, V> {

	/**
	 * Creates a cache decorator
	 * @param cache the decorated cache
	 */
	protected BlockingBasicCacheDecorator(BasicCache<K, V> cache) {
		super(cache);
	}

	/**
	 * Returns the value from the specified future.
	 * Default behaviour waits indefinitely.
	 * @param <T> the future value type
	 * @param future a future value
	 * @return the value from the specified future.
	 */
	protected <T> T join(CompletableFuture<T> future) {
		try {
			return future.get();
		} catch (ExecutionException e) {
			throw new CompletionException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CancellationException();
		}
	}

	@Override
	public void clear() {
		this.join(this.clearAsync());
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsKey(Object key) {
		return this.join(this.containsKeyAsync((K) key));
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return this.join(this.computeAsync(key, remappingFunction));
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.join(this.computeAsync(key, remappingFunction, lifespan, lifespanUnit));
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.join(this.computeAsync(key, remappingFunction, lifespan, lifespanUnit));
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return this.join(this.computeIfAbsentAsync(key, mappingFunction));
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.join(this.computeIfAbsentAsync(key, mappingFunction, lifespan, lifespanUnit));
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.join(this.computeIfAbsentAsync(key, mappingFunction, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit));
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return this.join(this.computeIfPresentAsync(key, remappingFunction));
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.join(this.computeIfPresentAsync(key, remappingFunction, lifespan, lifespanUnit));
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.join(this.computeIfPresentAsync(key, remappingFunction, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit));
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key) {
		return this.join(this.getAsync((K) key));
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return this.join(this.mergeAsync(key, value, remappingFunction));
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.join(this.mergeAsync(key, value, remappingFunction, lifespan, lifespanUnit));
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.join(this.mergeAsync(key, value, remappingFunction, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit));
	}

	@Override
	public V put(K key, V value) {
		return this.join(this.putAsync(key, value));
	}

	@Override
	public V put(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		return this.join(this.putAsync(key, value, lifespan, lifespanUnit));
	}

	@Override
	public V put(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.join(this.putAsync(key, value, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit));
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		this.join(this.putAllAsync(map));
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map, long lifespan, TimeUnit lifespanUnit) {
		this.join(this.putAllAsync(map, lifespan, lifespanUnit));
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		this.join(this.putAllAsync(map, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit));
	}

	@Override
	public V putIfAbsent(K key, V value) {
		return this.join(this.putIfAbsentAsync(key, value));
	}

	@Override
	public V putIfAbsent(K key, V value, long lifespan, TimeUnit unit) {
		return this.join(this.putIfAbsentAsync(key, value, lifespan, unit));
	}

	@Override
	public V putIfAbsent(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.join(this.putIfAbsentAsync(key, value, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit));
	}

	@Override
	public V remove(Object key) {
		return this.join(this.removeAsync(key));
	}

	@Override
	public boolean remove(Object key, Object value) {
		return this.join(this.removeAsync(key, value));
	}

	@Override
	public V replace(K key, V value) {
		return this.join(this.replaceAsync(key, value));
	}

	@Override
	public V replace(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		return this.join(this.replaceAsync(key, value, lifespan, lifespanUnit));
	}

	@Override
	public V replace(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.join(this.replaceAsync(key, value, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit));
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		return this.join(this.replaceAsync(key, oldValue, newValue));
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue, long lifespan, TimeUnit lifespanUnit) {
		return this.join(this.replaceAsync(key, oldValue, newValue, lifespan, lifespanUnit));
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.join(this.replaceAsync(key, oldValue, newValue, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit));
	}

	@Override
	public int size() {
		return this.join(this.sizeAsync()).intValue();
	}
}
