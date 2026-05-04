/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import javax.transaction.xa.Xid;

import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.MetadataValue;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.client.hotrod.impl.InternalRemoteCache;
import org.infinispan.client.hotrod.transaction.manager.RemoteXid;
import org.infinispan.commons.tx.TransactionImpl;
import org.infinispan.commons.util.concurrent.CompletableFutures;
import org.wildfly.clustering.cache.infinispan.remote.transaction.TransactionKey;
import org.wildfly.clustering.cache.infinispan.transaction.TransactionContextFactory;
import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.function.Function;

/**
 * A remote cache that performs locking reads if a transaction is active.
 * @param <K> the cache key type
 * @param <V> the cache value type
 * @author Paul Ferraro
 */
public class ReadForUpdateRemoteCache<K, V> extends RemoteCacheDecorator<K, V> {
	static final System.Logger LOGGER = System.getLogger(ReadForUpdateRemoteCache.class.getName());

	private static final Synchronization UNSUCCESSFUL = new Synchronization() {
		@Override
		public void beforeCompletion() {
		}

		@Override
		public void afterCompletion(int status) {
		}
	};
	private static final Xid INITIAL_TX_ID = RemoteXid.create(UUID.randomUUID());

	interface SynchronizationFactory extends Function<Xid, CompletableFuture<Synchronization>>, Synchronization {
	}

	private final InternalRemoteCache<K, V> cache;
	private final BiFunction<K, Transaction, CompletableFuture<Synchronization>> syncFactory;
	private final TransactionContextFactory contextFactory;

	/**
	 * Decorates a cache with read-for-update semantics.
	 * @param container the container of this cache
	 * @param cache a remote cache.
	 */
	public ReadForUpdateRemoteCache(RemoteCacheContainer container, RemoteCache<K, V> cache) {
		this(container, (InternalRemoteCache<K, V>) cache, Integer.MAX_VALUE);
	}

	/**
	 * Decorates a cache with read-for-update semantics.
	 * @param container the container of this cache
	 * @param cache a remote cache.
	 * @param maxRetries the maximum number of times a read operation should attempt to create tx entry before giving up.
	 */
	ReadForUpdateRemoteCache(RemoteCacheContainer container, RemoteCache<K, V> cache, int maxRetries) {
		this(container, (InternalRemoteCache<K, V>) cache, Integer.max(maxRetries, 0));
	}

	private ReadForUpdateRemoteCache(RemoteCacheContainer container, InternalRemoteCache<K, V> cache, int maxRetries) {
		this(container, cache, maxRetries, TransactionContextFactory.of(cache.getTransactionManager()));
	}

	@SuppressWarnings("unchecked")
	private ReadForUpdateRemoteCache(RemoteCacheContainer container, InternalRemoteCache<K, V> cache, int maxRetries, TransactionContextFactory contextFactory) {
		this(container, cache, contextFactory, new BiFunction<>() {
			private final RemoteCache<TransactionKey<K>, Xid> putCache = (RemoteCache<TransactionKey<K>, Xid>) cache.noFlags().withFlags(Flag.FORCE_RETURN_VALUE, Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD);
			private final RemoteCache<TransactionKey<K>, Xid> removeCache = (RemoteCache<TransactionKey<K>, Xid>) cache.noFlags().withFlags(Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD);
			private final Duration maxTxDuration = Duration.ofMillis(cache.getRemoteCacheContainer().getConfiguration().transactionTimeout());

			@Override
			public CompletableFuture<Synchronization> apply(K key, Transaction suspendedTx) {
				RemoteCache<TransactionKey<K>, Xid> putCache = this.putCache;
				RemoteCache<TransactionKey<K>, Xid> removeCache = this.removeCache;
				TransactionKey<K> currentTxKey = new TransactionKey<>(key);
				Xid currentTxId = ((TransactionImpl) suspendedTx).getXid();
				long maxTxDurationMillis = this.maxTxDuration.toMillis();
				Instant timeout = Instant.now().plus(this.maxTxDuration);
				int maxAttempts = maxRetries + 1;
				AtomicInteger attempts = new AtomicInteger(0);
				return new SynchronizationFactory() {
					@Override
					public CompletableFuture<Synchronization> apply(Xid txId) {
						if (txId == null) {
							LOGGER.log(System.Logger.Level.TRACE, "Locked {0} for read by {1} after {2} attempts", key, currentTxId, attempts);
							return CompletableFuture.completedFuture(this);
						}
						if (txId != INITIAL_TX_ID) {
							if (currentTxId.equals(txId)) {
								LOGGER.log(System.Logger.Level.TRACE, "{0} already locked for read by {1}", key, currentTxId);
								return CompletableFutures.completedNull();
							}
							if (attempts.incrementAndGet() == maxAttempts) {
								LOGGER.log(System.Logger.Level.TRACE, "Failed to lock {0} for read by {1} after {2} attempts", key, currentTxId, maxAttempts);
								return CompletableFuture.completedFuture(UNSUCCESSFUL);
							}
							if (Instant.now().isAfter(timeout)) {
								LOGGER.log(System.Logger.Level.TRACE, "Failed to lock {0} for read by {1} after {2} ms", key, currentTxId, maxTxDurationMillis);
								return CompletableFuture.failedFuture(new TimeoutException());
							}
							LOGGER.log(System.Logger.Level.TRACE, "Fail to lock {0} for read by {1}, still locked by {2}", key, currentTxId, txId);
							Thread.yield();
						}
						LOGGER.log(System.Logger.Level.TRACE, "Locking {0} for read by {1}", key, currentTxId);
						return putCache.putIfAbsentAsync(currentTxKey, currentTxId, maxTxDurationMillis, TimeUnit.MILLISECONDS).thenCompose(this);
					}

					@Override
					public void beforeCompletion() {
						// Do nothing
					}

					@Override
					public void afterCompletion(int status) {
						// Remove TX entry (outside of TX scope) without blocking
						try (Context<Transaction> context = contextFactory.suspendWithContext()) {
							LOGGER.log(System.Logger.Level.TRACE, "Unlocking {0} for read by {1}", key, currentTxId);
							removeCache.removeAsync(currentTxKey, currentTxId);
						}
					}
				}.apply(INITIAL_TX_ID);
			}
		});
	}

	private ReadForUpdateRemoteCache(RemoteCacheContainer container, InternalRemoteCache<K, V> cache, TransactionContextFactory contextFactory, BiFunction<K, Transaction, CompletableFuture<Synchronization>> syncFactory) {
		super(container, cache, new UnaryOperator<>() {
			@Override
			public InternalRemoteCache<K, V> apply(InternalRemoteCache<K, V> decorated) {
				return new ReadForUpdateRemoteCache<>(container, decorated, contextFactory, syncFactory);
			}
		});
		this.cache = cache;
		this.contextFactory = contextFactory;
		this.syncFactory = syncFactory;
	}

	@Override
	public CompletableFuture<V> getAsync(K key) {
		return this.readForUpdateAsync(RemoteCache::getAsync, key);
	}

	@Override
	public CompletableFuture<MetadataValue<V>> getWithMetadataAsync(K key) {
		return this.readForUpdateAsync(RemoteCache::getWithMetadataAsync, key);
	}

	private <T> CompletableFuture<T> readForUpdateAsync(BiFunction<RemoteCache<K, V>, K, CompletableFuture<T>> operation, K key) {
		try (Context<Transaction> suspended = this.contextFactory.suspendWithContext()) {
			Transaction suspendedTx = suspended.get();
			return (suspendedTx == null) ? operation.apply(this.cache, key) : this.syncFactory.apply(key, suspendedTx).thenCompose(synchronization -> {
				if ((synchronization != UNSUCCESSFUL) && (synchronization != null)) {
					try (Context<Transaction> resumed = this.contextFactory.resumeWithContext(suspendedTx)) {
						resumed.get().registerSynchronization(synchronization);
					} catch (RollbackException e) {
						synchronization.afterCompletion(Status.STATUS_MARKED_ROLLBACK);
					} catch (SystemException e) {
						throw new IllegalStateException(e);
					}
				}
				return (synchronization != UNSUCCESSFUL) ? operation.apply(this.cache, key) : CompletableFutures.completedNull();
			});
		}
	}

	@Override
	public CompletableFuture<Map<K, V>> getAllAsync(Set<?> keys) {
		if (keys.isEmpty()) return CompletableFutures.completedEmptyMap();
		try (Context<Transaction> suspended = this.contextFactory.suspendWithContext()) {
			Transaction suspendedTx = suspended.get();
			if (suspendedTx != null) {
				Map<K, Synchronization> synchronizations = new ConcurrentHashMap<>();
				CompletableFuture<Void> stage = new CompletableFuture<>();
				AtomicInteger remaining = new AtomicInteger(keys.size());
				for (Object rawKey : keys) {
					@SuppressWarnings("unchecked")
					K key = (K) rawKey;
					this.syncFactory.apply(key, suspendedTx).whenComplete((synchronization, exception) -> {
						if (exception != null) {
							stage.completeExceptionally(exception);
						} else {
							if (synchronization != null) {
								synchronizations.put(key, synchronization);
							}
							if (remaining.decrementAndGet() == 0) {
								stage.complete(null);
							}
						}
					});
				}
				return stage.thenCompose(ignore -> {
					Set<?> lockedKeys = new HashSet<>(keys);
					for (Map.Entry<K, Synchronization> entry : synchronizations.entrySet()) {
						Synchronization synchronization = entry.getValue();
						if (synchronization != UNSUCCESSFUL) {
							try (Context<Transaction> resumed = this.contextFactory.resumeWithContext(suspendedTx)) {
								resumed.get().registerSynchronization(synchronization);
							} catch (RollbackException e) {
								synchronization.afterCompletion(Status.STATUS_MARKED_ROLLBACK);
							} catch (SystemException e) {
								throw new IllegalStateException(e);
							}
						} else {
							lockedKeys.remove(entry.getKey());
						}
					}
					return !lockedKeys.isEmpty() ? super.getAllAsync(lockedKeys) : CompletableFutures.completedEmptyMap();
				});
			}
			return super.getAllAsync(keys);
		}
	}
}
