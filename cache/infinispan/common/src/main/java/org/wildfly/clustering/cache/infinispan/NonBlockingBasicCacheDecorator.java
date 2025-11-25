/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.infinispan.commons.api.BasicCache;

/**
 * A {@link BasicCache} decorator that delegates both synchronous and asynchronous operations.
 * Synchronous methods delegate directly, since these could be non-blocking.
 * @author Paul Ferraro
 * @param <K> cache key type
 * @param <V> cache value type
 */
public class NonBlockingBasicCacheDecorator<K, V> extends AbstractBasicCacheDecorator<K, V> {

	private final BasicCache<K, V> cache;

	/**
	 * Creates a cache decorator
	 * @param cache the decorated cache
	 */
	protected NonBlockingBasicCacheDecorator(BasicCache<K, V> cache) {
		super(cache);
		this.cache = cache;
	}

	@Override
	public void clear() {
		this.cache.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return this.cache.containsKey(key);
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return this.cache.compute(key, remappingFunction);
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.cache.compute(key, remappingFunction, lifespan, lifespanUnit);
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.cache.compute(key, remappingFunction, lifespan, lifespanUnit);
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return this.cache.computeIfAbsent(key, mappingFunction);
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.cache.computeIfAbsent(key, mappingFunction, lifespan, lifespanUnit);
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.cache.computeIfAbsent(key, mappingFunction, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit);
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return this.cache.computeIfPresent(key, remappingFunction);
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.cache.computeIfPresent(key, remappingFunction, lifespan, lifespanUnit);
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.cache.computeIfPresent(key, remappingFunction, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit);
	}

	@Override
	public V get(Object key) {
		return this.cache.get(key);
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return this.cache.merge(key, value, remappingFunction);
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		return this.cache.merge(key, value, remappingFunction, lifespan, lifespanUnit);
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.cache.merge(key, value, remappingFunction, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit);
	}

	@Override
	public V put(K key, V value) {
		return this.cache.put(key, value);
	}

	@Override
	public V put(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		return this.cache.put(key, value, lifespan, lifespanUnit);
	}

	@Override
	public V put(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.cache.put(key, value, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		this.cache.putAll(map);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map, long lifespan, TimeUnit lifespanUnit) {
		this.cache.putAll(map, lifespan, lifespanUnit);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		this.cache.putAll(map, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit);
	}

	@Override
	public V putIfAbsent(K key, V value) {
		return this.cache.putIfAbsent(key, value);
	}

	@Override
	public V putIfAbsent(K key, V value, long lifespan, TimeUnit unit) {
		return this.cache.putIfAbsent(key, value, lifespan, unit);
	}

	@Override
	public V putIfAbsent(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.cache.putIfAbsent(key, value, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit);
	}

	@Override
	public V remove(Object key) {
		return this.cache.remove(key);
	}

	@Override
	public boolean remove(Object key, Object value) {
		return this.cache.remove(key, value);
	}

	@Override
	public V replace(K key, V value) {
		return this.cache.replace(key, value);
	}

	@Override
	public V replace(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		return this.cache.replace(key, value, lifespan, lifespanUnit);
	}

	@Override
	public V replace(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.cache.replace(key, value, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit);
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		return this.cache.replace(key, oldValue, newValue);
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue, long lifespan, TimeUnit lifespanUnit) {
		return this.cache.replace(key, oldValue, newValue, lifespan, lifespanUnit);
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
		return this.cache.replace(key, oldValue, newValue, lifespan, lifespanUnit, maxIdleTime, maxIdleTimeUnit);
	}

	@Override
	public int size() {
		return this.cache.size();
	}
}
