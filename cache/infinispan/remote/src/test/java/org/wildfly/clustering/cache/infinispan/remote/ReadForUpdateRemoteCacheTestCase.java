/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.assertj.core.api.Assertions;
import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.wildfly.clustering.function.BiFunction;

/**
 * @author Paul Ferraro
 */
public class ReadForUpdateRemoteCacheTestCase {

	@Test
	public void getAsync() {
		RemoteCache<UUID, String> ignoreReturnCache = Mockito.mock(RemoteCache.class);
		RemoteCache<UUID, String> forceReturnCache = Mockito.mock(RemoteCache.class);
		UUID key = UUID.randomUUID();
		String expected = "expected";

		Mockito.doReturn(forceReturnCache).when(ignoreReturnCache).withFlags(AdditionalMatchers.aryEq(new Flag[] { Flag.FORCE_RETURN_VALUE }));
		Mockito.doReturn(CompletableFuture.completedFuture(expected)).when(forceReturnCache).computeIfPresentAsync(ArgumentMatchers.eq(key), ArgumentMatchers.same(BiFunction.latter()), ArgumentMatchers.eq(0L), ArgumentMatchers.any(), ArgumentMatchers.eq(0L), ArgumentMatchers.any());

		RemoteCache<UUID, String> subject = new ReadForUpdateRemoteCache<>(ignoreReturnCache);
		CompletableFuture<String> result = subject.getAsync(key);

		Assertions.assertThat(result).isCompletedWithValue(expected);
	}

	@Test
	public void getAllAsync() {
		RemoteCache<UUID, String> ignoreReturnCache = Mockito.mock(RemoteCache.class);
		RemoteCache<UUID, String> forceReturnCache = Mockito.mock(RemoteCache.class);
		UUID existingKey = UUID.randomUUID();
		UUID missingKey = UUID.randomUUID();
		UUID exceptionKey = UUID.randomUUID();
		String expected = "existing";
		Exception exception = new Exception();

		Mockito.doReturn(forceReturnCache).when(ignoreReturnCache).withFlags(AdditionalMatchers.aryEq(new Flag[] { Flag.FORCE_RETURN_VALUE }));
		Mockito.doReturn(CompletableFuture.completedFuture(expected)).when(forceReturnCache).computeIfPresentAsync(ArgumentMatchers.eq(existingKey), ArgumentMatchers.same(BiFunction.latter()), ArgumentMatchers.eq(0L), ArgumentMatchers.any(), ArgumentMatchers.eq(0L), ArgumentMatchers.any());
		Mockito.doReturn(CompletableFuture.completedFuture(null)).when(forceReturnCache).computeIfPresentAsync(ArgumentMatchers.eq(missingKey), ArgumentMatchers.same(BiFunction.latter()), ArgumentMatchers.eq(0L), ArgumentMatchers.any(), ArgumentMatchers.eq(0L), ArgumentMatchers.any());

		RemoteCache<UUID, String> subject = new ReadForUpdateRemoteCache<>(ignoreReturnCache);

		Assertions.assertThat(subject.getAllAsync(Set.of(existingKey, missingKey))).isCompletedWithValue(Map.of(existingKey, expected));

		Mockito.doReturn(CompletableFuture.failedFuture(exception)).when(forceReturnCache).computeIfPresentAsync(ArgumentMatchers.eq(exceptionKey), ArgumentMatchers.same(BiFunction.latter()), ArgumentMatchers.eq(0L), ArgumentMatchers.any(), ArgumentMatchers.eq(0L), ArgumentMatchers.any());

		Assertions.assertThat(subject.getAllAsync(Set.of(existingKey, missingKey, exceptionKey))).isCompletedExceptionally();
	}
}
