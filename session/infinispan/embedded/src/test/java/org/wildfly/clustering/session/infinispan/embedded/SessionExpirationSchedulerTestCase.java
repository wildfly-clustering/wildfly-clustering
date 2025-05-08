/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.embedded;

import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.server.expiration.ExpirationMetaData;
import org.wildfly.clustering.server.scheduler.Scheduler;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.metadata.ImmutableSessionMetaDataFactory;

/**
 * Unit test for {@link SessionExpirationScheduler}.
 *
 * @author Paul Ferraro
 */
public class SessionExpirationSchedulerTestCase {
	@Test
	public void test() throws InterruptedException {
		Supplier<Batch> batchFactory = mock(Supplier.class);
		Batch batch = mock(Batch.class);
		Predicate<String> remover = mock(Predicate.class);
		ImmutableSessionMetaDataFactory<Object> metaDataFactory = mock(ImmutableSessionMetaDataFactory.class);
		ImmutableSessionMetaData immortalSessionMetaData = mock(ImmutableSessionMetaData.class);
		ImmutableSessionMetaData expiringSessionMetaData = mock(ImmutableSessionMetaData.class);
		ImmutableSessionMetaData canceledSessionMetaData = mock(ImmutableSessionMetaData.class);
		ImmutableSessionMetaData busySessionMetaData = mock(ImmutableSessionMetaData.class);
		String immortalSessionId = "immortal";
		String expiringSessionId = "expiring";
		String canceledSessionId = "canceled";
		String busySessionId = "busy";

		when(batchFactory.get()).thenReturn(batch);

		when(immortalSessionMetaData.isImmortal()).thenReturn(true);
		when(expiringSessionMetaData.isImmortal()).thenReturn(false);
		when(canceledSessionMetaData.isImmortal()).thenReturn(false);
		when(busySessionMetaData.isImmortal()).thenReturn(false);
		when(expiringSessionMetaData.getTimeout()).thenReturn(Duration.ofMillis(1L));
		when(busySessionMetaData.getTimeout()).thenReturn(Duration.ofMillis(1L));
		when(canceledSessionMetaData.getTimeout()).thenReturn(Duration.ofSeconds(100L));

		Instant now = Instant.now();
		doCallRealMethod().when(expiringSessionMetaData).getLastAccessTime();
		doReturn(now).when(expiringSessionMetaData).getLastAccessEndTime();
		doCallRealMethod().when(canceledSessionMetaData).getLastAccessTime();
		doReturn(now).when(canceledSessionMetaData).getLastAccessEndTime();
		doCallRealMethod().when(busySessionMetaData).getLastAccessTime();
		doReturn(now).when(busySessionMetaData).getLastAccessEndTime();
		doReturn(true).when(remover).test(expiringSessionId);
		doReturn(false, true).when(remover).test(busySessionId);

		try (Scheduler<String, ExpirationMetaData> scheduler = new SessionExpirationScheduler<>("test", Supplier.of(batch), metaDataFactory, remover, Duration.ZERO)) {
			scheduler.schedule(immortalSessionId, immortalSessionMetaData);
			scheduler.schedule(canceledSessionId, canceledSessionMetaData);
			scheduler.schedule(expiringSessionId, expiringSessionMetaData);
			scheduler.schedule(busySessionId, busySessionMetaData);

			scheduler.cancel(canceledSessionId);

			TimeUnit.MILLISECONDS.sleep(500);
		}

		verify(remover, times(1)).test(expiringSessionId);
		verify(remover, times(2)).test(busySessionId);
		verify(remover, never()).test(immortalSessionId);
		verify(remover, never()).test(canceledSessionId);
		verify(batch, times(3)).close();
	}
}
