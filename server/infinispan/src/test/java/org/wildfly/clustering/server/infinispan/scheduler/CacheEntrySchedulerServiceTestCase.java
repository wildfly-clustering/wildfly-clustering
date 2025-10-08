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

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.server.scheduler.Scheduler;
import org.wildfly.clustering.server.scheduler.SchedulerService;

/**
 * Unit test for {@link CacheEntrySchedulerService}.
 * @author Paul Ferraro
 */
public class CacheEntrySchedulerServiceTestCase {
	private final Random random = new Random();

	@Test
	public void schedule() {
		SchedulerService<UUID, Instant> scheduler = mock(SchedulerService.class);
		BiFunction<UUID, Object, Instant> mapper = mock(BiFunction.class);

		@SuppressWarnings("resource")
		Scheduler<UUID, Instant> cacheEntryScheduler = new CacheEntrySchedulerService<>(scheduler, mapper);

		UUID id = UUID.randomUUID();
		Instant now = Instant.now();

		cacheEntryScheduler.schedule(id, now);

		verifyNoInteractions(mapper);
		verify(scheduler).schedule(id, now);
		verifyNoMoreInteractions(scheduler);
	}

	@Test
	public void scheduleEntry() {
		SchedulerService<UUID, Instant> scheduler = mock(SchedulerService.class);
		BiFunction<UUID, Object, Instant> mapper = mock(BiFunction.class);

		@SuppressWarnings("resource")
		CacheEntryScheduler<Key<UUID>, Object> cacheEntryScheduler = new CacheEntrySchedulerService<>(scheduler, mapper);

		UUID id = UUID.randomUUID();
		Key<UUID> key = mock(Key.class);
		Object value = mock(Object.class);
		Instant now = Instant.now();

		doReturn(id).when(key).getId();
		doReturn(now).when(mapper).apply(id, value);

		cacheEntryScheduler.scheduleEntry(Map.entry(key, value));

		verify(mapper).apply(id, value);
		verifyNoMoreInteractions(mapper);
		verify(scheduler).schedule(id, now);
		verifyNoMoreInteractions(scheduler);
	}

	@Test
	public void cancel() {
		SchedulerService<UUID, Instant> scheduler = mock(SchedulerService.class);
		BiFunction<UUID, Object, Instant> mapper = mock(BiFunction.class);

		@SuppressWarnings("resource")
		Scheduler<UUID, Instant> cacheEntryScheduler = new CacheEntrySchedulerService<>(scheduler, mapper);

		UUID id = UUID.randomUUID();

		cacheEntryScheduler.cancel(id);

		verifyNoInteractions(mapper);
		verify(scheduler).cancel(id);
		verifyNoMoreInteractions(scheduler);
	}

	@Test
	public void cancelKey() {
		SchedulerService<UUID, Instant> scheduler = mock(SchedulerService.class);
		BiFunction<UUID, Object, Instant> mapper = mock(BiFunction.class);

		@SuppressWarnings("resource")
		CacheEntryScheduler<Key<UUID>, Object> cacheEntryScheduler = new CacheEntrySchedulerService<>(scheduler, mapper);

		UUID id = UUID.randomUUID();
		Key<UUID> key = mock(Key.class);

		doReturn(id).when(key).getId();

		cacheEntryScheduler.cancelKey(key);

		verifyNoInteractions(mapper);
		verify(scheduler).cancel(id);
		verifyNoMoreInteractions(scheduler);
	}

	@Test
	public void contains() {
		SchedulerService<UUID, Instant> scheduler = mock(SchedulerService.class);
		BiFunction<UUID, Object, Instant> mapper = mock(BiFunction.class);

		@SuppressWarnings("resource")
		Scheduler<UUID, Instant> cacheEntryScheduler = new CacheEntrySchedulerService<>(scheduler, mapper);

		UUID id = UUID.randomUUID();
		boolean expected = this.random.nextBoolean();

		doReturn(expected).when(scheduler).contains(id);

		assertThat(cacheEntryScheduler.contains(id)).isEqualTo(expected);

		verifyNoInteractions(mapper);
		verify(scheduler).contains(id);
		verifyNoMoreInteractions(scheduler);
	}

	@Test
	public void containsKey() {
		SchedulerService<UUID, Instant> scheduler = mock(SchedulerService.class);
		BiFunction<UUID, Object, Instant> mapper = mock(BiFunction.class);

		@SuppressWarnings("resource")
		CacheEntryScheduler<Key<UUID>, Object> cacheEntryScheduler = new CacheEntrySchedulerService<>(scheduler, mapper);

		UUID id = UUID.randomUUID();
		Key<UUID> key = mock(Key.class);
		boolean expected = this.random.nextBoolean();

		doReturn(id).when(key).getId();
		doReturn(expected).when(scheduler).contains(id);

		assertThat(cacheEntryScheduler.containsKey(key)).isEqualTo(expected);

		verifyNoInteractions(mapper);
		verify(scheduler).contains(id);
		verifyNoMoreInteractions(scheduler);
	}

	@Test
	public void lifecycle() {
		SchedulerService<UUID, Instant> scheduler = mock(SchedulerService.class);
		BiFunction<UUID, Object, Instant> mapper = mock(BiFunction.class);

		try (CacheEntrySchedulerService<UUID, Key<UUID>, Object, Instant> cacheEntryScheduler = new CacheEntrySchedulerService<>(scheduler, mapper)) {

			cacheEntryScheduler.start();

			verifyNoInteractions(mapper);
			verify(scheduler).start();
			verifyNoMoreInteractions(scheduler);

			cacheEntryScheduler.stop();

			verifyNoInteractions(mapper);
			verify(scheduler).stop();
			verifyNoMoreInteractions(scheduler);
		}

		verifyNoInteractions(mapper);
		verify(scheduler).close();
		verifyNoMoreInteractions(scheduler);
	}
}
