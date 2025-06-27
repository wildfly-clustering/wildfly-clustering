/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.AdditionalMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.MetadataValue;
import org.infinispan.client.hotrod.RemoteCache;
import org.junit.jupiter.api.Test;

/**
 * @author Paul Ferraro
 */
public class ReadForUpdateRemoteCacheTestCase {

	@Test
	public void getAsync() throws SystemException {
		RemoteCache<UUID, String> cache = mock(RemoteCache.class);
		TransactionManager tm = mock(TransactionManager.class);
		UUID missingKey = UUID.randomUUID();
		UUID existingKey = UUID.randomUUID();
		UUID exceptionKey = UUID.randomUUID();
		Exception exception = new Exception();

		MetadataValue<String> staleMetadataValue = mock(MetadataValue.class);
		String staleValue = "stale";
		long staleVersion = existingKey.getLeastSignificantBits();

		MetadataValue<String> expectedMetadataValue = mock(MetadataValue.class);
		String expectedValue = "expected";
		long expectedVersion = existingKey.getMostSignificantBits();

		doReturn(tm).when(cache).getTransactionManager();
		doReturn(Status.STATUS_ACTIVE).when(tm).getStatus();
		doReturn(cache).when(cache).withFlags(aryEq(new Flag[] { Flag.FORCE_RETURN_VALUE }));

		doReturn(staleValue).when(staleMetadataValue).getValue();
		doReturn(staleVersion).when(staleMetadataValue).getVersion();

		doReturn(expectedValue).when(expectedMetadataValue).getValue();
		doReturn(expectedVersion).when(expectedMetadataValue).getVersion();

		doReturn(CompletableFuture.completedFuture(null)).when(cache).getWithMetadataAsync(missingKey);
		doReturn(CompletableFuture.failedFuture(exception)).when(cache).getWithMetadataAsync(exceptionKey);
		doReturn(CompletableFuture.completedFuture(staleMetadataValue), CompletableFuture.completedFuture(expectedMetadataValue)).when(cache).getWithMetadataAsync(existingKey);

		doReturn(CompletableFuture.completedFuture(Boolean.FALSE)).when(cache).replaceWithVersionAsync(existingKey, staleValue, staleVersion);
		doReturn(CompletableFuture.completedFuture(Boolean.TRUE)).when(cache).replaceWithVersionAsync(existingKey, expectedValue, expectedVersion);
		doReturn(CompletableFuture.completedFuture(Boolean.FALSE)).when(cache).replaceWithVersionAsync(existingKey, staleValue, staleVersion, 0L, TimeUnit.SECONDS, 0L, TimeUnit.SECONDS);
		doReturn(CompletableFuture.completedFuture(Boolean.TRUE)).when(cache).replaceWithVersionAsync(existingKey, expectedValue, expectedVersion, 0L, TimeUnit.SECONDS, 0L, TimeUnit.SECONDS);

		RemoteCache<UUID, String> subject = new ReadForUpdateRemoteCache<>(cache);

		assertThat(subject.getAsync(exceptionKey)).isCompletedExceptionally();
		assertThat(subject.getAsync(missingKey)).isCompletedWithValue(null);
		assertThat(subject.getAsync(existingKey)).isCompletedWithValue(expectedValue);
	}

	@Test
	public void getAllAsync() throws SystemException {
		RemoteCache<UUID, String> cache = mock(RemoteCache.class);
		TransactionManager tm = mock(TransactionManager.class);
		UUID missingKey = UUID.randomUUID();
		UUID existingKey = UUID.randomUUID();
		UUID exceptionKey = UUID.randomUUID();
		Exception exception = new Exception();

		MetadataValue<String> staleMetadataValue = mock(MetadataValue.class);
		String staleValue = "stale";
		long staleVersion = existingKey.getLeastSignificantBits();

		MetadataValue<String> expectedMetadataValue = mock(MetadataValue.class);
		String expectedValue = "expected";
		long expectedVersion = existingKey.getMostSignificantBits();

		doReturn(tm).when(cache).getTransactionManager();
		doReturn(Status.STATUS_ACTIVE).when(tm).getStatus();
		doReturn(cache).when(cache).withFlags(aryEq(new Flag[] { Flag.FORCE_RETURN_VALUE }));

		doReturn(staleValue).when(staleMetadataValue).getValue();
		doReturn(staleVersion).when(staleMetadataValue).getVersion();

		doReturn(expectedValue).when(expectedMetadataValue).getValue();
		doReturn(expectedVersion).when(expectedMetadataValue).getVersion();

		doReturn(CompletableFuture.completedFuture(null)).when(cache).getWithMetadataAsync(missingKey);
		doReturn(CompletableFuture.failedFuture(exception)).when(cache).getWithMetadataAsync(exceptionKey);
		doReturn(CompletableFuture.completedFuture(staleMetadataValue), CompletableFuture.completedFuture(expectedMetadataValue)).when(cache).getWithMetadataAsync(existingKey);

		doReturn(CompletableFuture.completedFuture(Boolean.FALSE)).when(cache).replaceWithVersionAsync(existingKey, staleValue, staleVersion);
		doReturn(CompletableFuture.completedFuture(Boolean.TRUE)).when(cache).replaceWithVersionAsync(existingKey, expectedValue, expectedVersion);
		doReturn(CompletableFuture.completedFuture(Boolean.FALSE)).when(cache).replaceWithVersionAsync(existingKey, staleValue, staleVersion, 0L, TimeUnit.SECONDS, 0L, TimeUnit.SECONDS);
		doReturn(CompletableFuture.completedFuture(Boolean.TRUE)).when(cache).replaceWithVersionAsync(existingKey, expectedValue, expectedVersion, 0L, TimeUnit.SECONDS, 0L, TimeUnit.SECONDS);

		RemoteCache<UUID, String> subject = new ReadForUpdateRemoteCache<>(cache);

		assertThat(subject.getAllAsync(Set.of(existingKey, missingKey))).isCompletedWithValue(Map.of(existingKey, expectedValue));
		assertThat(subject.getAllAsync(Set.of(existingKey, missingKey, exceptionKey))).isCompletedExceptionally();
	}
}
