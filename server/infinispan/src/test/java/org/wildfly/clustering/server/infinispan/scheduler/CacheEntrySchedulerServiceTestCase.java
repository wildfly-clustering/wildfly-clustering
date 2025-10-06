/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.CacheEntryLocator;
import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.function.Supplier;

/**
 * Unit test for {@link CacheEntrySchedulerService}.
 * @author Paul Ferraro
 */
public class CacheEntrySchedulerServiceTestCase {
	private final Random random = new Random();

	@Test
	public void schedule() {
		org.wildfly.clustering.server.scheduler.SchedulerService<UUID, Instant> scheduler = mock(org.wildfly.clustering.server.scheduler.SchedulerService.class);
		CacheEntryLocator<UUID, Object> locator = mock(CacheEntryLocator.class);
		BiFunction<UUID, Object, Instant> metaData = mock(BiFunction.class);
		Supplier<Batch> batchFactory = mock(Supplier.class);
		CacheEntrySchedulerService.Configuration<UUID, Key<UUID>, Object, Instant> configuration = mock(CacheEntrySchedulerService.Configuration.class);

		doReturn(scheduler).when(configuration).getSchedulerService();
		doReturn(locator).when(configuration).getLocator();
		doReturn(metaData).when(configuration).getMetaData();
		doReturn(batchFactory).when(configuration).getBatchFactory();
		doCallRealMethod().when(configuration).getStartTask();
		doCallRealMethod().when(configuration).getStopTask();

		@SuppressWarnings("resource")
		Scheduler<UUID, Instant> cacheEntryScheduler = new CacheEntrySchedulerService<>(configuration);

		Batch batch = mock(Batch.class);
		UUID id = UUID.randomUUID();
		Object value = mock(Object.class);
		Instant now = Instant.now();

		doReturn(batch).when(batchFactory).get();
		doReturn(value).when(locator).findValue(id);
		doReturn(now).when(metaData).apply(id, value);

		cacheEntryScheduler.schedule(id);

		verify(batchFactory).get();
		verifyNoMoreInteractions(batchFactory);
		verify(batch).close();
		verifyNoMoreInteractions(batch);
		verify(locator).findValue(id);
		verifyNoMoreInteractions(locator);
		verify(metaData).apply(id, value);
		verifyNoMoreInteractions(metaData);
		verify(scheduler).schedule(id, now);
		verifyNoMoreInteractions(scheduler);
	}

	@Test
	public void scheduleWithMetaData() {
		org.wildfly.clustering.server.scheduler.SchedulerService<UUID, Instant> scheduler = mock(org.wildfly.clustering.server.scheduler.SchedulerService.class);
		CacheEntryLocator<UUID, Object> locator = mock(CacheEntryLocator.class);
		BiFunction<UUID, Object, Instant> metaData = mock(BiFunction.class);
		CacheEntrySchedulerService.Configuration<UUID, Key<UUID>, Object, Instant> configuration = mock(CacheEntrySchedulerService.Configuration.class);

		doReturn(scheduler).when(configuration).getSchedulerService();
		doReturn(locator).when(configuration).getLocator();
		doReturn(metaData).when(configuration).getMetaData();
		doCallRealMethod().when(configuration).getStartTask();
		doCallRealMethod().when(configuration).getStopTask();

		@SuppressWarnings("resource")
		Scheduler<UUID, Instant> cacheEntryScheduler = new CacheEntrySchedulerService<>(configuration);

		UUID id = UUID.randomUUID();
		Instant now = Instant.now();

		cacheEntryScheduler.schedule(id, now);

		verifyNoInteractions(locator);
		verifyNoInteractions(metaData);
		verify(scheduler).schedule(id, now);
		verifyNoMoreInteractions(scheduler);
	}

	@Test
	public void scheduleKey() {
		org.wildfly.clustering.server.scheduler.SchedulerService<UUID, Instant> scheduler = mock(org.wildfly.clustering.server.scheduler.SchedulerService.class);
		CacheEntryLocator<UUID, Object> locator = mock(CacheEntryLocator.class);
		BiFunction<UUID, Object, Instant> metaData = mock(BiFunction.class);
		Supplier<Batch> batchFactory = mock(Supplier.class);
		CacheEntrySchedulerService.Configuration<UUID, Key<UUID>, Object, Instant> configuration = mock(CacheEntrySchedulerService.Configuration.class);

		doReturn(scheduler).when(configuration).getSchedulerService();
		doReturn(locator).when(configuration).getLocator();
		doReturn(metaData).when(configuration).getMetaData();
		doReturn(batchFactory).when(configuration).getBatchFactory();
		doCallRealMethod().when(configuration).getStartTask();
		doCallRealMethod().when(configuration).getStopTask();

		@SuppressWarnings("resource")
		CacheEntryScheduler<Key<UUID>, Object> cacheEntryScheduler = new CacheEntrySchedulerService<>(configuration);

		Batch batch = mock(Batch.class);
		UUID id = UUID.randomUUID();
		Key<UUID> key = mock(Key.class);
		Object value = mock(Object.class);
		Instant now = Instant.now();

		doReturn(batch).when(batchFactory).get();
		doReturn(id).when(key).getId();
		doReturn(value).when(locator).findValue(id);
		doReturn(now).when(metaData).apply(id, value);

		cacheEntryScheduler.scheduleKey(key);

		verify(batchFactory).get();
		verifyNoMoreInteractions(batchFactory);
		verify(batch).close();
		verifyNoMoreInteractions(batch);
		verify(locator).findValue(id);
		verifyNoMoreInteractions(locator);
		verify(metaData).apply(id, value);
		verifyNoMoreInteractions(metaData);
		verify(scheduler).schedule(id, now);
		verifyNoMoreInteractions(scheduler);
	}

	@Test
	public void scheduleEntry() {
		org.wildfly.clustering.server.scheduler.SchedulerService<UUID, Instant> scheduler = mock(org.wildfly.clustering.server.scheduler.SchedulerService.class);
		CacheEntryLocator<UUID, Object> locator = mock(CacheEntryLocator.class);
		BiFunction<UUID, Object, Instant> metaData = mock(BiFunction.class);
		CacheEntrySchedulerService.Configuration<UUID, Key<UUID>, Object, Instant> configuration = mock(CacheEntrySchedulerService.Configuration.class);

		doReturn(scheduler).when(configuration).getSchedulerService();
		doReturn(locator).when(configuration).getLocator();
		doReturn(metaData).when(configuration).getMetaData();
		doCallRealMethod().when(configuration).getStartTask();
		doCallRealMethod().when(configuration).getStopTask();

		@SuppressWarnings("resource")
		CacheEntryScheduler<Key<UUID>, Object> cacheEntryScheduler = new CacheEntrySchedulerService<>(configuration);

		UUID id = UUID.randomUUID();
		Key<UUID> key = mock(Key.class);
		Object value = mock(Object.class);
		Instant now = Instant.now();

		doReturn(id).when(key).getId();
		doReturn(now).when(metaData).apply(id, value);

		cacheEntryScheduler.scheduleEntry(Map.entry(key, value));

		verifyNoInteractions(locator);
		verify(metaData).apply(id, value);
		verifyNoMoreInteractions(metaData);
		verify(scheduler).schedule(id, now);
		verifyNoMoreInteractions(scheduler);
	}

	@Test
	public void cancel() {
		org.wildfly.clustering.server.scheduler.SchedulerService<UUID, Instant> scheduler = mock(org.wildfly.clustering.server.scheduler.SchedulerService.class);
		CacheEntryLocator<UUID, Object> locator = mock(CacheEntryLocator.class);
		BiFunction<UUID, Object, Instant> metaData = mock(BiFunction.class);
		CacheEntrySchedulerService.Configuration<UUID, Key<UUID>, Object, Instant> configuration = mock(CacheEntrySchedulerService.Configuration.class);

		doReturn(scheduler).when(configuration).getSchedulerService();
		doReturn(locator).when(configuration).getLocator();
		doReturn(metaData).when(configuration).getMetaData();
		doCallRealMethod().when(configuration).getStartTask();
		doCallRealMethod().when(configuration).getStopTask();

		@SuppressWarnings("resource")
		Scheduler<UUID, Instant> cacheEntryScheduler = new CacheEntrySchedulerService<>(configuration);

		UUID id = UUID.randomUUID();

		cacheEntryScheduler.cancel(id);

		verifyNoInteractions(locator);
		verifyNoInteractions(metaData);
		verify(scheduler).cancel(id);
		verifyNoMoreInteractions(scheduler);
	}

	@Test
	public void cancelKey() {
		org.wildfly.clustering.server.scheduler.SchedulerService<UUID, Instant> scheduler = mock(org.wildfly.clustering.server.scheduler.SchedulerService.class);
		CacheEntryLocator<UUID, Object> locator = mock(CacheEntryLocator.class);
		BiFunction<UUID, Object, Instant> metaData = mock(BiFunction.class);
		CacheEntrySchedulerService.Configuration<UUID, Key<UUID>, Object, Instant> configuration = mock(CacheEntrySchedulerService.Configuration.class);

		doReturn(scheduler).when(configuration).getSchedulerService();
		doReturn(locator).when(configuration).getLocator();
		doReturn(metaData).when(configuration).getMetaData();
		doCallRealMethod().when(configuration).getStartTask();
		doCallRealMethod().when(configuration).getStopTask();

		@SuppressWarnings("resource")
		CacheEntryScheduler<Key<UUID>, Object> cacheEntryScheduler = new CacheEntrySchedulerService<>(configuration);

		UUID id = UUID.randomUUID();
		Key<UUID> key = mock(Key.class);

		doReturn(id).when(key).getId();

		cacheEntryScheduler.cancelKey(key);

		verifyNoInteractions(locator);
		verifyNoInteractions(metaData);
		verify(scheduler).cancel(id);
		verifyNoMoreInteractions(scheduler);
	}

	@Test
	public void contains() {
		org.wildfly.clustering.server.scheduler.SchedulerService<UUID, Instant> scheduler = mock(org.wildfly.clustering.server.scheduler.SchedulerService.class);
		CacheEntryLocator<UUID, Object> locator = mock(CacheEntryLocator.class);
		BiFunction<UUID, Object, Instant> metaData = mock(BiFunction.class);
		CacheEntrySchedulerService.Configuration<UUID, Key<UUID>, Object, Instant> configuration = mock(CacheEntrySchedulerService.Configuration.class);

		doReturn(scheduler).when(configuration).getSchedulerService();
		doReturn(locator).when(configuration).getLocator();
		doReturn(metaData).when(configuration).getMetaData();
		doCallRealMethod().when(configuration).getStartTask();
		doCallRealMethod().when(configuration).getStopTask();

		@SuppressWarnings("resource")
		Scheduler<UUID, Instant> cacheEntryScheduler = new CacheEntrySchedulerService<>(configuration);

		UUID id = UUID.randomUUID();
		boolean expected = this.random.nextBoolean();

		doReturn(expected).when(scheduler).contains(id);

		assertThat(cacheEntryScheduler.contains(id)).isEqualTo(expected);

		verifyNoInteractions(locator);
		verifyNoInteractions(metaData);
		verify(scheduler).contains(id);
		verifyNoMoreInteractions(scheduler);
	}

	@Test
	public void containsKey() {
		org.wildfly.clustering.server.scheduler.SchedulerService<UUID, Instant> scheduler = mock(org.wildfly.clustering.server.scheduler.SchedulerService.class);
		CacheEntryLocator<UUID, Object> locator = mock(CacheEntryLocator.class);
		BiFunction<UUID, Object, Instant> metaData = mock(BiFunction.class);
		CacheEntrySchedulerService.Configuration<UUID, Key<UUID>, Object, Instant> configuration = mock(CacheEntrySchedulerService.Configuration.class);

		doReturn(scheduler).when(configuration).getSchedulerService();
		doReturn(locator).when(configuration).getLocator();
		doReturn(metaData).when(configuration).getMetaData();
		doCallRealMethod().when(configuration).getStartTask();
		doCallRealMethod().when(configuration).getStopTask();

		@SuppressWarnings("resource")
		CacheEntryScheduler<Key<UUID>, Object> cacheEntryScheduler = new CacheEntrySchedulerService<>(configuration);

		UUID id = UUID.randomUUID();
		Key<UUID> key = mock(Key.class);
		boolean expected = this.random.nextBoolean();

		doReturn(id).when(key).getId();
		doReturn(expected).when(scheduler).contains(id);

		assertThat(cacheEntryScheduler.containsKey(key)).isEqualTo(expected);

		verifyNoInteractions(locator);
		verifyNoInteractions(metaData);
		verify(scheduler).contains(id);
		verifyNoMoreInteractions(scheduler);
	}

	@Test
	public void lifecycle() {
		org.wildfly.clustering.server.scheduler.SchedulerService<UUID, Instant> scheduler = mock(org.wildfly.clustering.server.scheduler.SchedulerService.class);
		CacheEntryLocator<UUID, Object> locator = mock(CacheEntryLocator.class);
		BiFunction<UUID, Object, Instant> metaData = mock(BiFunction.class);
		Consumer<CacheEntryScheduler<Key<UUID>, Object>> startTask = mock(Consumer.class);
		Consumer<CacheEntryScheduler<Key<UUID>, Object>> stopTask = mock(Consumer.class);

		CacheEntrySchedulerService.Configuration<UUID, Key<UUID>, Object, Instant> configuration = mock(CacheEntrySchedulerService.Configuration.class);

		doReturn(scheduler).when(configuration).getSchedulerService();
		doReturn(locator).when(configuration).getLocator();
		doReturn(metaData).when(configuration).getMetaData();
		doReturn(startTask).when(configuration).getStartTask();
		doReturn(stopTask).when(configuration).getStopTask();

		try (CacheEntrySchedulerService<UUID, Key<UUID>, Object, Instant> cacheEntryScheduler = new CacheEntrySchedulerService<>(configuration)) {
			verifyNoInteractions(startTask);
			verifyNoInteractions(stopTask);

			cacheEntryScheduler.start();

			verify(startTask).accept(cacheEntryScheduler);
			verifyNoMoreInteractions(startTask);
			verifyNoInteractions(stopTask);
			verifyNoInteractions(locator);
			verifyNoInteractions(metaData);
			verify(scheduler).start();
			verifyNoMoreInteractions(scheduler);

			cacheEntryScheduler.stop();

			verifyNoMoreInteractions(startTask);
			verify(stopTask).accept(cacheEntryScheduler);
			verifyNoMoreInteractions(stopTask);
			verifyNoInteractions(locator);
			verifyNoInteractions(metaData);
			verify(scheduler).stop();
			verifyNoMoreInteractions(scheduler);
		}

		verifyNoMoreInteractions(startTask);
		verifyNoMoreInteractions(stopTask);
		verifyNoInteractions(locator);
		verifyNoInteractions(metaData);
		verify(scheduler).close();
		verifyNoMoreInteractions(scheduler);
	}
}
