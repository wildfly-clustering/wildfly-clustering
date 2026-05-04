/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.AdditionalMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.transaction.xa.Xid;

import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.impl.InternalRemoteCache;
import org.infinispan.client.hotrod.transaction.manager.RemoteXid;
import org.infinispan.commons.tx.TransactionImpl;
import org.infinispan.commons.tx.XidImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.wildfly.clustering.cache.infinispan.remote.transaction.TransactionKey;

/**
 * @author Paul Ferraro
 */
public class ReadForUpdateRemoteCacheTestCase {

	@Test
	public void getAsync() throws SystemException, InvalidTransactionException {
		InternalRemoteCache<UUID, String> cache = mock(InternalRemoteCache.class);
		InternalRemoteCache<TransactionKey<UUID>, Xid> txPutCache = mock(InternalRemoteCache.class);
		InternalRemoteCache<TransactionKey<UUID>, Xid> txRemoveCache = mock(InternalRemoteCache.class);
		RemoteCacheContainer container = mock(RemoteCacheContainer.class);
		Configuration configuration = mock(Configuration.class);
		TransactionManager tm = mock(TransactionManager.class);
		TransactionImpl tx = new TransactionImpl() { };
		XidImpl txId = RemoteXid.create(UUID.randomUUID());
		tx.setXid(txId);
		XidImpl otherTxId = RemoteXid.create(UUID.randomUUID());

		UUID key = UUID.randomUUID();
		String value = "foo";
		TransactionKey<UUID> txKey = new TransactionKey<>(key);
		UUID missingKey = UUID.randomUUID();
		TransactionKey<UUID> missingTxKey = new TransactionKey<>(missingKey);
		UUID exceptionKey = UUID.randomUUID();
		TransactionKey<UUID> exceptionTxKey = new TransactionKey<>(exceptionKey);
		Exception exception = new Exception();
		long transactionTimeout = 1000L;

		doReturn(container).when(cache).getRemoteCacheContainer();
		doReturn(tm).when(cache).getTransactionManager();
		doReturn(configuration).when(container).getConfiguration();
		doReturn(transactionTimeout).when(configuration).transactionTimeout();
		doReturn(tm).when(cache).getTransactionManager();
		doReturn(tx).when(tm).getTransaction();
		doReturn(tx).when(tm).suspend();
		doReturn(cache).when(cache).noFlags();
		doReturn(txPutCache).when(cache).withFlags(aryEq(new Flag[] { Flag.FORCE_RETURN_VALUE, Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD }));
		doReturn(txRemoveCache).when(cache).withFlags(aryEq(new Flag[] { Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD }));

		CompletableFuture<Xid> future1 = new CompletableFuture<>();
		CompletableFuture<Xid> future2 = new CompletableFuture<>();

		doReturn(future1, future2, CompletableFuture.completedFuture(txId)).when(txPutCache).putIfAbsentAsync(txKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		doReturn(CompletableFuture.completedFuture(txId)).when(txPutCache).putIfAbsentAsync(missingTxKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		doReturn(CompletableFuture.completedFuture(txId)).when(txPutCache).putIfAbsentAsync(exceptionTxKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);

		doReturn(CompletableFuture.completedFuture(value)).when(cache).getAsync(key);
		doReturn(CompletableFuture.completedFuture(null)).when(cache).getAsync(missingKey);
		doReturn(CompletableFuture.failedFuture(exception)).when(cache).getAsync(exceptionKey);

		RemoteCache<UUID, String> subject = new ReadForUpdateRemoteCache<>(container, cache);

		verify(cache, atLeastOnce()).noFlags();
		verify(cache).withFlags(Flag.FORCE_RETURN_VALUE, Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD);
		verify(cache).withFlags(Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD);
		verify(cache, atLeastOnce()).getTransactionManager();
		verify(cache, atLeastOnce()).getRemoteCacheContainer();
		verifyNoMoreInteractions(cache);

		InOrder order = inOrder(tm, txPutCache, txRemoveCache, cache);

		CompletableFuture<String> result = subject.getAsync(key);

		assertThat(result).isNotCompleted();
		assertThat(tx.getEnlistedSynchronization()).isEmpty();

		order.verify(tm).suspend();
		order.verify(txPutCache).putIfAbsentAsync(txKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		order.verify(tm).resume(tx);
		order.verifyNoMoreInteractions();

		future1.complete(otherTxId);

		assertThat(result).isNotCompleted();
		assertThat(tx.getEnlistedSynchronization()).isEmpty();

		order.verify(txPutCache).putIfAbsentAsync(txKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		order.verifyNoMoreInteractions();

		future2.complete(null);

		assertThat(result).isCompletedWithValue(value);
		assertThat(tx.getEnlistedSynchronization()).hasSize(1);

		order.verify(cache).getAsync(key);
		order.verifyNoMoreInteractions();

		assertThat(subject.getAsync(missingKey)).isCompletedWithValue(null);
		assertThat(tx.getEnlistedSynchronization()).hasSize(1);

		order.verify(tm).suspend();
		order.verify(txPutCache).putIfAbsentAsync(missingTxKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		order.verify(cache).getAsync(missingKey);
		order.verify(tm).resume(tx);
		order.verifyNoMoreInteractions();

		assertThat(subject.getAsync(exceptionKey)).isCompletedExceptionally();
		assertThat(tx.getEnlistedSynchronization()).hasSize(1);

		order.verify(tm).suspend();
		order.verify(txPutCache).putIfAbsentAsync(exceptionTxKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		order.verify(cache).getAsync(exceptionKey);
		order.verify(tm).resume(tx);
		order.verifyNoMoreInteractions();

		Synchronization synchronization = tx.getEnlistedSynchronization().iterator().next();

		synchronization.beforeCompletion();

		order.verifyNoMoreInteractions();

		PrimitiveIterator.OfInt statuses = IntStream.of(Status.STATUS_COMMITTED, Status.STATUS_ROLLEDBACK).iterator();
		while (statuses.hasNext()) {
			synchronization.afterCompletion(statuses.nextInt());

			order.verify(tm).suspend();
			order.verify(txRemoveCache).removeAsync(txKey, txId);
			order.verify(tm).resume(tx);
			order.verifyNoMoreInteractions();
		}
	}

	@Test
	public void getAsyncTimeout() throws SystemException, InvalidTransactionException, InterruptedException {
		InternalRemoteCache<UUID, String> cache = mock(InternalRemoteCache.class);
		InternalRemoteCache<TransactionKey<UUID>, Xid> txPutCache = mock(InternalRemoteCache.class);
		InternalRemoteCache<TransactionKey<UUID>, Xid> txRemoveCache = mock(InternalRemoteCache.class);
		RemoteCacheContainer container = mock(RemoteCacheContainer.class);
		Configuration configuration = mock(Configuration.class);
		TransactionManager tm = mock(TransactionManager.class);
		TransactionImpl tx = new TransactionImpl() { };
		XidImpl txId = RemoteXid.create(UUID.randomUUID());
		tx.setXid(txId);
		XidImpl otherTxId = RemoteXid.create(UUID.randomUUID());

		UUID key = UUID.randomUUID();
		String value = "foo";
		TransactionKey<UUID> txKey = new TransactionKey<>(key);
		long transactionTimeout = 50L;

		doReturn(container).when(cache).getRemoteCacheContainer();
		doReturn(tm).when(cache).getTransactionManager();
		doReturn(configuration).when(container).getConfiguration();
		doReturn(transactionTimeout).when(configuration).transactionTimeout();
		doReturn(tm).when(cache).getTransactionManager();
		doReturn(tx).when(tm).getTransaction();
		doReturn(tx).when(tm).suspend();
		doReturn(cache).when(cache).noFlags();
		doReturn(txPutCache).when(cache).withFlags(aryEq(new Flag[] { Flag.FORCE_RETURN_VALUE, Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD }));
		doReturn(txRemoveCache).when(cache).withFlags(aryEq(new Flag[] { Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD }));

		CompletableFuture<Xid> future = new CompletableFuture<>();

		doReturn(future).when(txPutCache).putIfAbsentAsync(txKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		doReturn(CompletableFuture.completedFuture(value)).when(cache).getAsync(key);

		RemoteCache<UUID, String> subject = new ReadForUpdateRemoteCache<>(container, cache);

		verify(cache, atLeastOnce()).noFlags();
		verify(cache).withFlags(Flag.FORCE_RETURN_VALUE, Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD);
		verify(cache).withFlags(Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD);
		verify(cache, atLeastOnce()).getTransactionManager();
		verify(cache, atLeastOnce()).getRemoteCacheContainer();
		verifyNoMoreInteractions(cache);

		InOrder order = inOrder(tm, txPutCache, txRemoveCache, cache);

		CompletableFuture<String> result = subject.getAsync(key);

		assertThat(result).isNotCompleted();
		assertThat(tx.getEnlistedSynchronization()).isEmpty();

		order.verify(tm).suspend();
		order.verify(txPutCache).putIfAbsentAsync(txKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		order.verify(tm).resume(tx);
		order.verifyNoMoreInteractions();

		future.complete(otherTxId);

		assertThat(result).isNotCompleted();
		assertThat(tx.getEnlistedSynchronization()).isEmpty();

		TimeUnit.MILLISECONDS.sleep(50L);

		assertThat(result).isCompletedExceptionally();
		assertThat(tx.getEnlistedSynchronization()).isEmpty();

		order.verify(txPutCache, atLeastOnce()).putIfAbsentAsync(txKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		order.verifyNoMoreInteractions();
	}

	@Test
	public void tryGetAsync() throws SystemException, InvalidTransactionException {
		InternalRemoteCache<UUID, String> cache = mock(InternalRemoteCache.class);
		InternalRemoteCache<TransactionKey<UUID>, Xid> txPutCache = mock(InternalRemoteCache.class);
		InternalRemoteCache<TransactionKey<UUID>, Xid> txRemoveCache = mock(InternalRemoteCache.class);
		RemoteCacheContainer container = mock(RemoteCacheContainer.class);
		Configuration configuration = mock(Configuration.class);
		TransactionManager tm = mock(TransactionManager.class);
		TransactionImpl tx = new TransactionImpl() { };
		XidImpl txId = RemoteXid.create(UUID.randomUUID());
		tx.setXid(txId);
		XidImpl otherTxId = RemoteXid.create(UUID.randomUUID());

		UUID key = UUID.randomUUID();
		String value = "foo";
		TransactionKey<UUID> txKey = new TransactionKey<>(key);
		long transactionTimeout = 1000L;

		doReturn(container).when(cache).getRemoteCacheContainer();
		doReturn(tm).when(cache).getTransactionManager();
		doReturn(configuration).when(container).getConfiguration();
		doReturn(transactionTimeout).when(configuration).transactionTimeout();
		doReturn(tm).when(cache).getTransactionManager();
		doReturn(tx).when(tm).getTransaction();
		doReturn(tx).when(tm).suspend();
		doReturn(cache).when(cache).noFlags();
		doReturn(txPutCache).when(cache).withFlags(aryEq(new Flag[] { Flag.FORCE_RETURN_VALUE, Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD }));
		doReturn(txRemoveCache).when(cache).withFlags(aryEq(new Flag[] { Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD }));

		CompletableFuture<Xid> future1 = new CompletableFuture<>();
		CompletableFuture<Xid> future2 = new CompletableFuture<>();

		doReturn(future1, future2, CompletableFuture.completedFuture(txId)).when(txPutCache).putIfAbsentAsync(txKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		doReturn(CompletableFuture.completedFuture(value)).when(cache).getAsync(key);

		RemoteCache<UUID, String> subject = new ReadForUpdateRemoteCache<>(container, cache, 0);

		verify(cache, atLeastOnce()).noFlags();
		verify(cache).withFlags(Flag.FORCE_RETURN_VALUE, Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD);
		verify(cache).withFlags(Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD);
		verify(cache, atLeastOnce()).getTransactionManager();
		verify(cache, atLeastOnce()).getRemoteCacheContainer();
		verifyNoMoreInteractions(cache);

		InOrder order = inOrder(tm, txPutCache, txRemoveCache, cache);

		CompletableFuture<String> result = subject.getAsync(key);

		assertThat(result).isNotCompleted();
		assertThat(tx.getEnlistedSynchronization()).isEmpty();

		order.verify(tm).suspend();
		order.verify(txPutCache).putIfAbsentAsync(txKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		order.verify(tm).resume(tx);
		order.verifyNoMoreInteractions();

		future1.complete(otherTxId);

		assertThat(result).isCompletedWithValue(null);
		assertThat(tx.getEnlistedSynchronization()).isEmpty();

		order.verifyNoMoreInteractions();

		result = subject.getAsync(key);

		assertThat(result).isNotCompleted();
		assertThat(tx.getEnlistedSynchronization()).isEmpty();

		order.verify(tm).suspend();
		order.verify(txPutCache).putIfAbsentAsync(txKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		order.verify(tm).resume(tx);
		order.verifyNoMoreInteractions();

		future2.complete(null);

		assertThat(result).isCompletedWithValue(value);
		assertThat(tx.getEnlistedSynchronization()).hasSize(1);

		order.verify(cache).getAsync(key);
		order.verifyNoMoreInteractions();

		Synchronization synchronization = tx.getEnlistedSynchronization().iterator().next();

		synchronization.beforeCompletion();

		order.verifyNoMoreInteractions();

		PrimitiveIterator.OfInt statuses = IntStream.of(Status.STATUS_COMMITTED, Status.STATUS_ROLLEDBACK).iterator();
		while (statuses.hasNext()) {
			synchronization.afterCompletion(statuses.nextInt());

			order.verify(tm).suspend();
			order.verify(txRemoveCache).removeAsync(txKey, txId);
			order.verify(tm).resume(tx);
			order.verifyNoMoreInteractions();
		}
	}

	@Test
	public void getAllAsync() throws SystemException, InvalidTransactionException {
		InternalRemoteCache<UUID, String> cache = mock(InternalRemoteCache.class);
		InternalRemoteCache<TransactionKey<UUID>, Xid> txPutCache = mock(InternalRemoteCache.class);
		InternalRemoteCache<TransactionKey<UUID>, Xid> txRemoveCache = mock(InternalRemoteCache.class);
		RemoteCacheContainer container = mock(RemoteCacheContainer.class);
		Configuration configuration = mock(Configuration.class);
		TransactionManager tm = mock(TransactionManager.class);
		TransactionImpl tx = new TransactionImpl() { };
		XidImpl txId = RemoteXid.create(UUID.randomUUID());
		tx.setXid(txId);
		XidImpl otherTxId = RemoteXid.create(UUID.randomUUID());

		UUID key = UUID.randomUUID();
		Map<UUID, String> value = Map.of(key, "foo");
		TransactionKey<UUID> txKey = new TransactionKey<>(key);
		UUID exceptionKey = UUID.randomUUID();
		TransactionKey<UUID> exceptionTxKey = new TransactionKey<>(exceptionKey);
		Exception exception = new Exception();
		long transactionTimeout = 1000L;

		doReturn(container).when(cache).getRemoteCacheContainer();
		doReturn(tm).when(cache).getTransactionManager();
		doReturn(configuration).when(container).getConfiguration();
		doReturn(transactionTimeout).when(configuration).transactionTimeout();
		doReturn(tm).when(cache).getTransactionManager();
		doReturn(tx).when(tm).getTransaction();
		doReturn(tx).when(tm).suspend();
		doReturn(cache).when(cache).noFlags();
		doReturn(txPutCache).when(cache).withFlags(aryEq(new Flag[] { Flag.FORCE_RETURN_VALUE, Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD }));
		doReturn(txRemoveCache).when(cache).withFlags(aryEq(new Flag[] { Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD }));

		CompletableFuture<Xid> future1 = new CompletableFuture<>();
		CompletableFuture<Xid> future2 = new CompletableFuture<>();

		doReturn(future1, future2, CompletableFuture.completedFuture(txId)).when(txPutCache).putIfAbsentAsync(txKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		doReturn(CompletableFuture.completedFuture(txId)).when(txPutCache).putIfAbsentAsync(exceptionTxKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);

		doReturn(CompletableFuture.completedFuture(value)).when(cache).getAllAsync(Set.of(key));
		doReturn(CompletableFuture.failedFuture(exception)).when(cache).getAllAsync(Set.of(key, exceptionKey));

		RemoteCache<UUID, String> subject = new ReadForUpdateRemoteCache<>(container, cache);

		verify(cache, atLeastOnce()).noFlags();
		verify(cache).withFlags(Flag.FORCE_RETURN_VALUE, Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD);
		verify(cache).withFlags(Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD);
		verify(cache, atLeastOnce()).getTransactionManager();
		verify(cache, atLeastOnce()).getRemoteCacheContainer();
		verifyNoMoreInteractions(cache);

		InOrder order = inOrder(tm, txPutCache, txRemoveCache, cache);

		CompletableFuture<Map<UUID, String>> result = subject.getAllAsync(Set.of(key));

		assertThat(result).isNotCompleted();
		assertThat(tx.getEnlistedSynchronization()).isEmpty();

		order.verify(tm).suspend();
		order.verify(txPutCache).putIfAbsentAsync(txKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		order.verify(tm).resume(tx);
		order.verifyNoMoreInteractions();

		future1.complete(otherTxId);

		assertThat(result).isNotCompleted();
		assertThat(tx.getEnlistedSynchronization()).isEmpty();

		order.verify(txPutCache).putIfAbsentAsync(txKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		order.verifyNoMoreInteractions();

		future2.complete(null);

		assertThat(result).isCompletedWithValue(value);
		assertThat(tx.getEnlistedSynchronization()).hasSize(1);

		order.verify(cache).getAllAsync(Set.of(key));

		assertThat(subject.getAllAsync(Set.of(key, exceptionKey))).isCompletedExceptionally();
		assertThat(tx.getEnlistedSynchronization()).hasSize(1);

		order.verify(tm).suspend();
		order.verify(txPutCache).putIfAbsentAsync(exceptionTxKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		order.verify(cache).getAllAsync(Set.of(key, exceptionKey));
		order.verify(tm).resume(tx);
		order.verifyNoMoreInteractions();

		Synchronization synchronization = tx.getEnlistedSynchronization().iterator().next();

		synchronization.beforeCompletion();

		order.verifyNoMoreInteractions();

		PrimitiveIterator.OfInt statuses = IntStream.of(Status.STATUS_COMMITTED, Status.STATUS_ROLLEDBACK).iterator();
		while (statuses.hasNext()) {
			synchronization.afterCompletion(statuses.nextInt());

			order.verify(tm).suspend();
			order.verify(txRemoveCache).removeAsync(txKey, txId);
			order.verify(tm).resume(tx);
			order.verifyNoMoreInteractions();
		}
	}

	@Test
	public void getAllAsyncTimeout() throws SystemException, InvalidTransactionException, InterruptedException {
		InternalRemoteCache<UUID, String> cache = mock(InternalRemoteCache.class);
		InternalRemoteCache<TransactionKey<UUID>, Xid> txPutCache = mock(InternalRemoteCache.class);
		InternalRemoteCache<TransactionKey<UUID>, Xid> txRemoveCache = mock(InternalRemoteCache.class);
		RemoteCacheContainer container = mock(RemoteCacheContainer.class);
		Configuration configuration = mock(Configuration.class);
		TransactionManager tm = mock(TransactionManager.class);
		TransactionImpl tx = new TransactionImpl() { };
		XidImpl txId = RemoteXid.create(UUID.randomUUID());
		tx.setXid(txId);
		XidImpl otherTxId = RemoteXid.create(UUID.randomUUID());

		UUID key = UUID.randomUUID();
		Map<UUID, String> value = Map.of(key, "foo");
		TransactionKey<UUID> txKey = new TransactionKey<>(key);
		long transactionTimeout = 50L;

		doReturn(container).when(cache).getRemoteCacheContainer();
		doReturn(tm).when(cache).getTransactionManager();
		doReturn(configuration).when(container).getConfiguration();
		doReturn(transactionTimeout).when(configuration).transactionTimeout();
		doReturn(tm).when(cache).getTransactionManager();
		doReturn(tx).when(tm).getTransaction();
		doReturn(tx).when(tm).suspend();
		doReturn(cache).when(cache).noFlags();
		doReturn(txPutCache).when(cache).withFlags(aryEq(new Flag[] { Flag.FORCE_RETURN_VALUE, Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD }));
		doReturn(txRemoveCache).when(cache).withFlags(aryEq(new Flag[] { Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD }));

		CompletableFuture<Xid> future = new CompletableFuture<>();

		doReturn(future).when(txPutCache).putIfAbsentAsync(txKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		doReturn(CompletableFuture.completedFuture(value)).when(cache).getAllAsync(Set.of(key));

		RemoteCache<UUID, String> subject = new ReadForUpdateRemoteCache<>(container, cache);

		verify(cache, atLeastOnce()).noFlags();
		verify(cache).withFlags(Flag.FORCE_RETURN_VALUE, Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD);
		verify(cache).withFlags(Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD);
		verify(cache, atLeastOnce()).getTransactionManager();
		verify(cache, atLeastOnce()).getRemoteCacheContainer();
		verifyNoMoreInteractions(cache);

		InOrder order = inOrder(tm, txPutCache, txRemoveCache, cache);

		CompletableFuture<Map<UUID, String>> result = subject.getAllAsync(Set.of(key));

		assertThat(result).isNotCompleted();
		assertThat(tx.getEnlistedSynchronization()).isEmpty();

		order.verify(tm).suspend();
		order.verify(txPutCache).putIfAbsentAsync(txKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		order.verify(tm).resume(tx);
		order.verifyNoMoreInteractions();

		future.complete(otherTxId);

		assertThat(result).isNotCompleted();
		assertThat(tx.getEnlistedSynchronization()).isEmpty();

		TimeUnit.MILLISECONDS.sleep(50L);

		assertThat(result).isCompletedExceptionally();
		assertThat(tx.getEnlistedSynchronization()).isEmpty();

		order.verify(txPutCache, atLeastOnce()).putIfAbsentAsync(txKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		order.verifyNoMoreInteractions();

	}

	@Test
	public void tryGetAllAsync() throws SystemException, InvalidTransactionException {
		InternalRemoteCache<UUID, String> cache = mock(InternalRemoteCache.class);
		InternalRemoteCache<TransactionKey<UUID>, Xid> txPutCache = mock(InternalRemoteCache.class);
		InternalRemoteCache<TransactionKey<UUID>, Xid> txRemoveCache = mock(InternalRemoteCache.class);
		RemoteCacheContainer container = mock(RemoteCacheContainer.class);
		Configuration configuration = mock(Configuration.class);
		TransactionManager tm = mock(TransactionManager.class);
		TransactionImpl tx = new TransactionImpl() { };
		XidImpl txId = RemoteXid.create(UUID.randomUUID());
		tx.setXid(txId);
		XidImpl otherTxId = RemoteXid.create(UUID.randomUUID());

		UUID key = UUID.randomUUID();
		Map<UUID, String> value = Map.of(key, "foo");
		TransactionKey<UUID> txKey = new TransactionKey<>(key);
		long transactionTimeout = 1000L;

		doReturn(container).when(cache).getRemoteCacheContainer();
		doReturn(tm).when(cache).getTransactionManager();
		doReturn(configuration).when(container).getConfiguration();
		doReturn(transactionTimeout).when(configuration).transactionTimeout();
		doReturn(tm).when(cache).getTransactionManager();
		doReturn(tx).when(tm).getTransaction();
		doReturn(tx).when(tm).suspend();
		doReturn(cache).when(cache).noFlags();
		doReturn(txPutCache).when(cache).withFlags(aryEq(new Flag[] { Flag.FORCE_RETURN_VALUE, Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD }));
		doReturn(txRemoveCache).when(cache).withFlags(aryEq(new Flag[] { Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD }));

		CompletableFuture<Xid> future1 = new CompletableFuture<>();
		CompletableFuture<Xid> future2 = new CompletableFuture<>();

		doReturn(future1, future2, CompletableFuture.completedFuture(txId)).when(txPutCache).putIfAbsentAsync(txKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		doReturn(CompletableFuture.completedFuture(value)).when(cache).getAllAsync(Set.of(key));

		RemoteCache<UUID, String> subject = new ReadForUpdateRemoteCache<>(container, cache, 0);

		verify(cache, atLeastOnce()).noFlags();
		verify(cache).withFlags(Flag.FORCE_RETURN_VALUE, Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD);
		verify(cache).withFlags(Flag.SKIP_LISTENER_NOTIFICATION, Flag.SKIP_CACHE_LOAD);
		verify(cache, atLeastOnce()).getTransactionManager();
		verify(cache, atLeastOnce()).getRemoteCacheContainer();
		verifyNoMoreInteractions(cache);

		InOrder order = inOrder(tm, txPutCache, txRemoveCache, cache);

		CompletableFuture<Map<UUID, String>> result = subject.getAllAsync(Set.of(key));

		assertThat(result).isNotCompleted();
		assertThat(tx.getEnlistedSynchronization()).isEmpty();

		order.verify(tm).suspend();
		order.verify(txPutCache).putIfAbsentAsync(txKey, txId, transactionTimeout, TimeUnit.MILLISECONDS);
		order.verify(tm).resume(tx);
		order.verifyNoMoreInteractions();

		future1.complete(otherTxId);

		assertThat(result).isCompletedWithValue(Map.of());
		assertThat(tx.getEnlistedSynchronization()).isEmpty();

		order.verifyNoMoreInteractions();
	}
}
