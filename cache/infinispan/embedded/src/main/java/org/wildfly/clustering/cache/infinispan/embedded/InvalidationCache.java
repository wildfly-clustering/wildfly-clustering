/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.metadata.Metadata;

/**
 * Workaround for issues with invalidation caches.
 * There are several types of cache write operations that require the current value:
 * <ul>
 * <li>Computational operations, e.g. {@link #compute(Object, BiFunction)}</li>
 * <li>Conditional operations, e.g. {@link #replace(Object, Object)}</li>
 * <li>Operations returning the previous value, e.g. {@link #put(Object, Object)}</li>
 * </ul>
 * In order to perform these types of operations correctly, an invalidation cache must fetch the current value from the cache store if the entry is not already present in the data container.
 * For whatever reason, Infinispan does not do this, which results in incorrect behavior.
 * @author Paul Ferraro
 * @param <K> the cache key type
 * @param <V> the cache value type
 */
public class InvalidationCache<K, V> extends AbstractAdvancedCache<K, V> implements AdvancedCache<K, V> {
	// Capture the flags
	private final Collection<Flag> flags;

	public InvalidationCache(Cache<K, V> cache) {
		this(cache.getAdvancedCache());
	}

	public InvalidationCache(AdvancedCache<K, V> cache) {
		this(cache, EnumSet.noneOf(Flag.class));
	}

	private InvalidationCache(AdvancedCache<K, V> cache, Collection<Flag> flags) {
		super(cache);
		this.flags = flags;
	}

	@Override
	public AdvancedCache<K, V> withFlags(Collection<Flag> flags) {
		AdvancedCache<K, V> cache = this.cache.withFlags(flags);
		return (cache != this.cache) ? new InvalidationCache<>(cache, flags) : this;
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, Metadata metadata) {
		if (this.flags.contains(Flag.SKIP_CACHE_LOAD)) return super.compute(key, remappingFunction, metadata);
		V oldValue = this.lockOnReadCache().get(key);
		V newValue = remappingFunction.apply(key, oldValue);
		return (oldValue != newValue) ? (this.update(key, oldValue, newValue, metadata) ? newValue : this.compute(key, remappingFunction, metadata)) : newValue;
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, Metadata metadata) {
		if (this.flags.contains(Flag.SKIP_CACHE_LOAD)) return super.computeIfAbsent(key, mappingFunction, metadata);
		V oldValue = this.lockOnReadCache().get(key);
		V newValue = (oldValue == null) ? mappingFunction.apply(key) : oldValue;
		return (oldValue != newValue) ? (this.update(key, oldValue, newValue, metadata) ? newValue : this.computeIfAbsent(key, mappingFunction, metadata)) : newValue;
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, Metadata metadata) {
		if (this.flags.contains(Flag.SKIP_CACHE_LOAD)) return super.computeIfPresent(key, remappingFunction, metadata);
		V oldValue = this.lockOnReadCache().get(key);
		V newValue = (oldValue != null) ? remappingFunction.apply(key, oldValue) : null;
		return (oldValue != newValue) ? (this.update(key, oldValue, newValue, metadata) ? newValue : this.computeIfPresent(key, remappingFunction, metadata)) : newValue;
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, Metadata metadata) {
		if (this.flags.contains(Flag.SKIP_CACHE_LOAD)) return super.merge(key, value, remappingFunction, metadata);
		V oldValue = this.lockOnReadCache().get(key);
		V newValue = (oldValue != null) ? remappingFunction.apply(oldValue, value) : value;
		return (oldValue != newValue) ? (this.update(key, oldValue, newValue, metadata) ? newValue : this.merge(key, value, remappingFunction, metadata)) : newValue;
	}

	@Override
	public V put(K key, V newValue, Metadata metadata) {
		if (this.flags.contains(Flag.IGNORE_RETURN_VALUES) || this.flags.contains(Flag.SKIP_CACHE_LOAD)) return super.put(key, newValue, metadata);
		V oldValue = this.lockOnReadCache().get(key);
		return this.update(key, oldValue, newValue, metadata) ? oldValue : this.put(key, newValue, metadata);
	}

	@Override
	public V putIfAbsent(K key, V newValue, Metadata metadata) {
		if (this.flags.contains(Flag.SKIP_CACHE_LOAD)) return super.putIfAbsent(key, newValue, metadata);
		V oldValue = this.lockOnReadCache().get(key);
		return (oldValue == null) ? (this.update(key, oldValue, newValue, metadata) ? oldValue : this.putIfAbsent(key, newValue, metadata)) : oldValue;
	}

	@Override
	public V remove(Object key) {
		// Even if we ignore return values, Infinispan neglects to delete from the store if entry does not exist locally
		if (this.flags.contains(Flag.SKIP_CACHE_LOAD)) return super.remove(key);
		@SuppressWarnings("unchecked")
		K typedKey = (K) key;
		V oldValue = this.lockOnReadCache().get(key);
		return (oldValue != null) ? (this.update(typedKey, oldValue, null, null) ? oldValue : this.remove(key)) : oldValue;
	}

	@Override
	public V replace(K key, V newValue, Metadata metadata) {
		if (this.flags.contains(Flag.SKIP_CACHE_LOAD)) return super.put(key, newValue, metadata);
		V oldValue = this.lockOnReadCache().get(key);
		return (oldValue != null) ? (this.update(key, oldValue, newValue, metadata) ? oldValue : this.replace(key, newValue, metadata)) : oldValue;
	}

	@Override
	public CompletableFuture<V> computeAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, Metadata metadata) {
		if (this.flags.contains(Flag.SKIP_CACHE_LOAD)) return super.computeAsync(key, remappingFunction, metadata);
		return this.lockOnReadCache().getAsync(key)
				.thenApply(oldValue -> new AbstractMap.SimpleImmutableEntry<>(oldValue, remappingFunction.apply(key, oldValue)))
				.thenCompose(entry -> this.updateAsync(key, entry.getKey(), entry.getValue(), metadata).thenApply(updated -> updated ? entry : null))
				.thenCompose(entry -> (entry != null) ? CompletableFuture.completedStage(entry.getValue()) : this.computeAsync(key, remappingFunction, metadata));
	}

	@Override
	public CompletableFuture<V> computeIfAbsentAsync(K key, Function<? super K, ? extends V> mappingFunction, Metadata metadata) {
		if (this.flags.contains(Flag.SKIP_CACHE_LOAD)) return super.computeIfAbsentAsync(key, mappingFunction, metadata);
		return this.lockOnReadCache().getAsync(key)
				.thenApply(oldValue -> new AbstractMap.SimpleImmutableEntry<>(oldValue, (oldValue == null) ? mappingFunction.apply(key) : oldValue))
				.thenCompose(entry -> (entry.getKey() != entry.getValue()) ? this.updateAsync(key, entry.getKey(), entry.getValue(), metadata).thenApply(updated -> updated ? entry : null) : CompletableFuture.completedStage(entry))
				.thenCompose(entry -> (entry != null) ? CompletableFuture.completedStage(entry.getValue()) : this.computeIfAbsentAsync(key, mappingFunction, metadata));
	}

	@Override
	public CompletableFuture<V> computeIfPresentAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, Metadata metadata) {
		if (this.flags.contains(Flag.SKIP_CACHE_LOAD)) return super.computeIfPresentAsync(key, remappingFunction, metadata);
		return this.lockOnReadCache().getAsync(key)
				.thenApply(oldValue -> new AbstractMap.SimpleImmutableEntry<>(oldValue, (oldValue != null) ? remappingFunction.apply(key, oldValue) : oldValue))
				.thenCompose(entry -> (entry.getKey() != entry.getValue()) ? this.updateAsync(key, entry.getKey(), entry.getValue(), metadata).thenApply(updated -> updated ? entry : null) : CompletableFuture.completedStage(entry))
				.thenCompose(entry -> (entry != null) ? CompletableFuture.completedStage(entry.getValue()) : this.computeIfPresentAsync(key, remappingFunction, metadata));
	}

	@Override
	public CompletableFuture<V> mergeAsync(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, Metadata metadata) {
		if (this.flags.contains(Flag.SKIP_CACHE_LOAD)) return super.mergeAsync(key, value, remappingFunction, metadata);
		return this.lockOnReadCache().getAsync(key)
				.thenApply(oldValue -> new AbstractMap.SimpleImmutableEntry<>(oldValue, (oldValue != null) ? remappingFunction.apply(oldValue, value) : value))
				.thenCompose(entry -> (entry.getKey() != entry.getValue()) ? this.updateAsync(key, entry.getKey(), entry.getValue(), metadata).thenApply(updated -> updated ? entry : null) : CompletableFuture.completedStage(entry))
				.thenCompose(entry -> (entry != null) ? CompletableFuture.completedStage(entry.getValue()) : this.mergeAsync(key, value, remappingFunction, metadata));
	}

	@Override
	public CompletableFuture<V> putAsync(K key, V value, Metadata metadata) {
		if (this.flags.contains(Flag.IGNORE_RETURN_VALUES) || this.flags.contains(Flag.SKIP_CACHE_LOAD)) return super.putAsync(key, value, metadata);
		return this.lockOnReadCache().getAsync(key)
				.thenApply(oldValue -> new AbstractMap.SimpleImmutableEntry<>(oldValue, value))
				.thenCompose(entry -> this.updateAsync(key, entry.getKey(), entry.getValue(), metadata).thenApply(updated -> updated ? entry : null))
				.thenCompose(entry -> (entry != null) ? CompletableFuture.completedStage(entry.getKey()) : this.putAsync(key, value, metadata));
	}

	@Override
	public CompletableFuture<V> putIfAbsentAsync(K key, V newValue, Metadata metadata) {
		if (this.flags.contains(Flag.SKIP_CACHE_LOAD)) return super.putIfAbsentAsync(key, newValue, metadata);
		return this.lockOnReadCache().getAsync(key)
				.thenApply(oldValue -> new AbstractMap.SimpleImmutableEntry<>(oldValue, (oldValue == null) ? newValue : oldValue))
				.thenCompose(entry -> (entry.getKey() != entry.getValue()) ? this.updateAsync(key, entry.getKey(), entry.getValue(), metadata).thenApply(updated -> updated ? entry : null) : CompletableFuture.completedStage(entry))
				.thenCompose(entry -> (entry != null) ? CompletableFuture.completedStage(entry.getKey()) : this.putIfAbsentAsync(key, newValue, metadata));
	}

	@Override
	public CompletableFuture<V> removeAsync(Object key) {
		// Even if we ignore return values, Infinispan neglects to delete from the store if entry does not exist locally
		if (this.flags.contains(Flag.SKIP_CACHE_LOAD)) return super.removeAsync(key);
		@SuppressWarnings("unchecked")
		K typedKey = (K) key;
		return this.lockOnReadCache().getAsync(typedKey)
				.thenCompose(value -> (value != null) ? this.updateAsync(typedKey, value, null, null).thenApply(updated -> updated ? Optional.of(value) : null) : CompletableFuture.completedStage(Optional.<V>empty()))
				.thenCompose(value -> (value != null) ? CompletableFuture.completedStage(value.orElse(null)) : this.removeAsync(key));
	}

	@Override
	public CompletableFuture<V> replaceAsync(K key, V newValue, Metadata metadata) {
		if (this.flags.contains(Flag.SKIP_CACHE_LOAD)) return super.replaceAsync(key, newValue, metadata);
		return this.lockOnReadCache().getAsync(key)
				.thenApply(oldValue -> new AbstractMap.SimpleImmutableEntry<>(oldValue, (oldValue != null) ? newValue : oldValue))
				.thenCompose(entry -> (entry.getKey() != entry.getValue()) ? this.updateAsync(key, entry.getKey(), entry.getValue(), metadata).thenApply(updated -> updated ? entry : null) : CompletableFuture.completedStage(entry))
				.thenCompose(entry -> (entry != null) ? CompletableFuture.completedStage(entry.getKey()) : this.replaceAsync(key, newValue, metadata));
	}

	@SuppressWarnings("unchecked")
	@Override
	public AdvancedCache rewrap(AdvancedCache cache) {
		return new InvalidationCache<>(cache, this.flags);
	}

	boolean hasCurrentTransaction() {
		TransactionManager tm = this.getTransactionManager();
		return (tm != null) && hasCurrentTransaction(tm);
	}

	static boolean hasCurrentTransaction(TransactionManager tm) {
		try {
			return tm.getStatus() == Status.STATUS_NO_TRANSACTION;
		} catch (SystemException e) {
			return false;
		}
	}

	AdvancedCache<K, V> lockOnReadCache() {
		return this.hasCurrentTransaction() ? this.cache.withFlags(Flag.FORCE_WRITE_LOCK) : this.cache;
	}

	private boolean update(K key, V oldValue, V newValue, Metadata metadata) {
		return (oldValue != null) ? ((newValue != null) ? this.cache.replace(key, oldValue, newValue, metadata) : this.cache.remove(key, oldValue)) : (newValue != null) ? (this.cache.putIfAbsent(key, newValue, metadata) == null) : true;
	}

	private CompletionStage<Boolean> updateAsync(K key, V oldValue, V newValue, Metadata metadata) {
		return (oldValue != null) ? ((newValue != null) ? this.cache.replaceAsync(key, oldValue, newValue, metadata) : this.cache.removeAsync(key, oldValue)) : (newValue != null) ? this.cache.putIfAbsentAsync(key, newValue, metadata).thenApply(Objects::isNull) : CompletableFuture.completedStage(true);
	}
}
