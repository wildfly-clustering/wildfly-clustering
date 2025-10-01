/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.infinispan.AdvancedCache;
import org.infinispan.cache.impl.AbstractDelegatingAdvancedCache;
import org.infinispan.configuration.cache.Configurations;
import org.infinispan.context.Flag;
import org.infinispan.metadata.EmbeddedMetadata;
import org.infinispan.metadata.Metadata;

/**
 * An abstract cache decorator.
 * @author Paul Ferraro
 * @param <K> the cache key type
 * @param <V> the cache value type
 */
public abstract class AbstractAdvancedCache<K, V> extends AbstractDelegatingAdvancedCache<K, V> {
	private final Metadata defaultMetadata;

	/**
	 * Creates a new cache decorator.
	 * @param cache the cache to which to delegate.
	 */
	protected AbstractAdvancedCache(AdvancedCache<K, V> cache) {
		super(cache);
		this.defaultMetadata = Configurations.newDefaultMetadata(cache.getCacheConfiguration());
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata).build();
		return this.compute(key, remappingFunction, metadata);
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.build();
		return this.compute(key, remappingFunction, metadata);
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.maxIdle(maxIdle, maxIdleUnit)
				.build();
		return this.compute(key, remappingFunction, metadata);
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata).build();
		return this.computeIfAbsent(key, mappingFunction, metadata);
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, long lifespan, TimeUnit lifespanUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.build();
		return this.computeIfAbsent(key, mappingFunction, metadata);
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.maxIdle(maxIdle, maxIdleUnit)
				.build();
		return this.computeIfAbsent(key, mappingFunction, metadata);
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata).build();
		return this.computeIfPresent(key, remappingFunction, metadata);
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.build();
		return this.computeIfPresent(key, remappingFunction, metadata);
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.maxIdle(maxIdle, maxIdleUnit)
				.build();
		return this.computeIfPresent(key, remappingFunction, metadata);
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata).build();
		return this.merge(key, value, remappingFunction, metadata);
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.build();
		return this.merge(key, value, remappingFunction, metadata);
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.maxIdle(maxIdle, maxIdleUnit)
				.build();
		return this.merge(key, value, remappingFunction, metadata);
	}

	@Override
	public V put(K key, V value) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata).build();
		return this.put(key, value, metadata);
	}

	@Override
	public V put(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.build();
		return this.put(key, value, metadata);
	}

	@Override
	public V put(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.maxIdle(maxIdle, maxIdleUnit)
				.build();
		return this.put(key, value, metadata);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> entries) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata).build();
		this.putAll(entries, metadata);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> entries, long lifespan, TimeUnit lifespanUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.build();
		this.putAll(entries, metadata);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> entries, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.maxIdle(maxIdle, maxIdleUnit)
				.build();
		this.putAll(entries, metadata);
	}

	@Override
	public void putForExternalRead(K key, V value) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata).build();
		this.putForExternalRead(key, value, metadata);
	}

	@Override
	public void putForExternalRead(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.build();
		this.putForExternalRead(key, value, metadata);
	}

	@Override
	public void putForExternalRead(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.maxIdle(maxIdle, maxIdleUnit)
				.build();
		this.putForExternalRead(key, value, metadata);
	}

	@Override
	public V putIfAbsent(K key, V value) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata).build();
		return this.putIfAbsent(key, value, metadata);
	}

	@Override
	public V putIfAbsent(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.build();
		return this.putIfAbsent(key, value, metadata);
	}

	@Override
	public V putIfAbsent(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.maxIdle(maxIdle, maxIdleUnit)
				.build();
		return this.putIfAbsent(key, value, metadata);
	}

	@Override
	public V replace(K key, V value) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata).build();
		return this.replace(key, value, metadata);
	}

	@Override
	public V replace(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.build();
		return this.replace(key, value, metadata);
	}

	@Override
	public V replace(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.maxIdle(maxIdle, maxIdleUnit)
				.build();
		return this.replace(key, value, metadata);
	}

	@Override
	public CompletableFuture<V> computeAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata).build();
		return this.computeAsync(key, remappingFunction, metadata);
	}

	@Override
	public CompletableFuture<V> computeAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.build();
		return this.computeAsync(key, remappingFunction, metadata);
	}

	@Override
	public CompletableFuture<V> computeAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.maxIdle(maxIdle, maxIdleUnit)
				.build();
		return this.computeAsync(key, remappingFunction, metadata);
	}

	@Override
	public CompletableFuture<V> computeIfAbsentAsync(K key, Function<? super K, ? extends V> mappingFunction) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata).build();
		return this.computeIfAbsentAsync(key, mappingFunction, metadata);
	}

	@Override
	public CompletableFuture<V> computeIfAbsentAsync(K key, Function<? super K, ? extends V> mappingFunction, long lifespan, TimeUnit lifespanUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.build();
		return this.computeIfAbsentAsync(key, mappingFunction, metadata);
	}

	@Override
	public CompletableFuture<V> computeIfAbsentAsync(K key, Function<? super K, ? extends V> mappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.maxIdle(maxIdle, maxIdleUnit)
				.build();
		return this.computeIfAbsentAsync(key, mappingFunction, metadata);
	}

	@Override
	public CompletableFuture<V> computeIfPresentAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata).build();
		return this.computeIfPresentAsync(key, remappingFunction, metadata);
	}

	@Override
	public CompletableFuture<V> computeIfPresentAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.build();
		return this.computeIfPresentAsync(key, remappingFunction, metadata);
	}

	@Override
	public CompletableFuture<V> computeIfPresentAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.maxIdle(maxIdle, maxIdleUnit)
				.build();
		return this.computeIfPresentAsync(key, remappingFunction, metadata);
	}

	@Override
	public CompletableFuture<V> mergeAsync(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata).build();
		return this.mergeAsync(key, value, remappingFunction, metadata);
	}

	@Override
	public CompletableFuture<V> mergeAsync(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.build();
		return this.mergeAsync(key, value, remappingFunction, metadata);
	}

	@Override
	public CompletableFuture<V> mergeAsync(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.maxIdle(maxIdle, maxIdleUnit)
				.build();
		return this.mergeAsync(key, value, remappingFunction, metadata);
	}

	@Override
	public CompletableFuture<V> putAsync(K key, V value) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata).build();
		return this.putAsync(key, value, metadata);
	}

	@Override
	public CompletableFuture<V> putAsync(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.build();
		return this.putAsync(key, value, metadata);
	}

	@Override
	public CompletableFuture<V> putAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.maxIdle(maxIdle, maxIdleUnit)
				.build();
		return this.putAsync(key, value, metadata);
	}

	@Override
	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> entries) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata).build();
		return this.putAllAsync(entries, metadata);
	}

	@Override
	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> entries, long lifespan, TimeUnit lifespanUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.build();
		return this.putAllAsync(entries, metadata);
	}

	@Override
	public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> entries, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.maxIdle(maxIdle, maxIdleUnit)
				.build();
		return this.putAllAsync(entries, metadata);
	}

	@Override
	public CompletableFuture<V> putIfAbsentAsync(K key, V value) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata).build();
		return this.putIfAbsentAsync(key, value, metadata);
	}

	@Override
	public CompletableFuture<V> putIfAbsentAsync(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.build();
		return this.putIfAbsentAsync(key, value, metadata);
	}

	@Override
	public CompletableFuture<V> putIfAbsentAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.maxIdle(maxIdle, maxIdleUnit)
				.build();
		return this.putIfAbsentAsync(key, value, metadata);
	}

	@Override
	public CompletableFuture<V> replaceAsync(K key, V value) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata).build();
		return this.replaceAsync(key, value, metadata);
	}

	@Override
	public CompletableFuture<V> replaceAsync(K key, V value, long lifespan, TimeUnit lifespanUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.build();
		return this.replaceAsync(key, value, metadata);
	}

	@Override
	public CompletableFuture<V> replaceAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
		Metadata metadata = new EmbeddedMetadata.Builder().merge(this.defaultMetadata)
				.lifespan(lifespan, lifespanUnit)
				.maxIdle(maxIdle, maxIdleUnit)
				.build();
		return this.replaceAsync(key, value, metadata);
	}

	@Override
	public AdvancedCache<K, V> withFlags(Flag flag) {
		return this.withFlags(EnumSet.of(flag));
	}

	@Override
	public AdvancedCache<K, V> withFlags(Flag... flags) {
		return this.withFlags(Set.of(flags));
	}
}
