/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.MetadataValue;
import org.infinispan.client.hotrod.RemoteCache;
import org.wildfly.clustering.function.BooleanSupplier;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Supplier;

/**
 * A remote cache that performs locking reads if a transaction is active.
 * @param <K> the cache key type
 * @param <V> the cache value type
 * @author Paul Ferraro
 */
public class ReadForUpdateRemoteCache<K, V> extends AbstractRemoteCache<K, V> {
	private final Function<MetadataValue<V>, V> value;
	private final RemoteCache<K, V> forceReturnCache;
	private final BooleanSupplier currentTransation;

	/**
	 * Creates a read-for-update remote cache decorator.
	 * @param cache the decorated remote cache.
	 */
	public ReadForUpdateRemoteCache(RemoteCache<K, V> cache) {
		super(cache);
		Function<MetadataValue<V>, V> value = MetadataValue::getValue;
		this.value = value.orDefault(Objects::nonNull, Supplier.empty());
		this.forceReturnCache = cache.withFlags(Flag.FORCE_RETURN_VALUE);
		TransactionManager tm = cache.getTransactionManager();
		this.currentTransation = (tm != null) ? new BooleanSupplier() {
			@Override
			public boolean getAsBoolean() {
				try {
					return tm.getStatus() != Status.STATUS_NO_TRANSACTION;
				} catch (SystemException e) {
					return false;
				}
			}
		} : BooleanSupplier.FALSE;
	}

	@Override
	public CompletableFuture<V> getAsync(K key) {
		return this.currentTransation.getAsBoolean() ? this.getWithMetadataAsync(key).thenApply(this.value) : super.getAsync(key);
	}

	@Override
	public CompletableFuture<MetadataValue<V>> getWithMetadataAsync(K key) {
		CompletableFuture<MetadataValue<V>> result = super.getWithMetadataAsync(key);
		// HotRod lacks support for Flag.FORCE_WRITE_LOCK, so simulate this by using a subsequent replace operation
		return this.currentTransation.getAsBoolean() ? result.thenCompose(metadataValue -> {
			CompletableFuture<MetadataValue<V>> completed = CompletableFuture.completedFuture(metadataValue);
			if (metadataValue == null) return completed;
			V value = metadataValue.getValue();
			long version = metadataValue.getVersion();
			return this.forceReturnCache.replaceWithVersionAsync(key, value, version)
					.thenCompose(replaced -> replaced.booleanValue() ? completed : this.getWithMetadataAsync(key));
		}) : result;
	}

	@Override
	public CompletableFuture<Map<K, V>> getAllAsync(Set<?> keys) {
		if (keys.isEmpty()) return CompletableFuture.completedFuture(Map.of());
		if (!this.currentTransation.getAsBoolean()) return super.getAllAsync(keys);
		AtomicInteger remaining = new AtomicInteger(keys.size());
		Map<K, V> entries = new ConcurrentHashMap<>();
		CompletableFuture<Map<K, V>> result = new CompletableFuture<>();
		for (Object key : keys) {
			@SuppressWarnings("unchecked")
			K typedKey = (K) key;
			this.getAsync(typedKey).whenComplete((value, exception) -> {
				if (exception != null) {
					result.completeExceptionally(exception);
				} else {
					if (value != null) {
						entries.put(typedKey, value);
					}
					if (remaining.decrementAndGet() == 0) {
						result.complete(entries);
					}
				}
			});
		}
		return result;
	}

	@Override
	public RemoteCache<K, V> apply(RemoteCache<K, V> cache) {
		return new ReadForUpdateRemoteCache<>(cache);
	}
}
