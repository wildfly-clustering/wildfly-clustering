/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.scheduler.Scheduler;
import org.wildfly.common.function.Functions;

/**
 * @author Paul Ferraro
 */
public class LocalSchedulerTestCase {

	static LocalSchedulerConfiguration<UUID> configuration(ScheduledEntries<UUID, Instant> entries, Predicate<UUID> task) {
		return new LocalSchedulerConfiguration<>() {
			@Override
			public Supplier<ScheduledEntries<UUID, Instant>> getScheduledEntriesFactory() {
				return Functions.constantSupplier(entries);
			}

			@Override
			public Predicate<UUID> getTask() {
				return task;
			}

			@Override
			public ThreadFactory getThreadFactory() {
				return Thread::new;
			}

			@Override
			public Duration getCloseTimeout() {
				return Duration.ZERO;
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Test
	public void successfulTask() throws InterruptedException {
		ScheduledEntries<UUID, Instant> entries = mock(ScheduledEntries.class);
		Predicate<UUID> task = mock(Predicate.class);

		Map.Entry<UUID, Instant> entry = Map.entry(UUID.randomUUID(), Instant.now());
		Map.Entry<UUID, Instant> nullEntry = null;
		List<Map.Entry<UUID, Instant>> entryList = new ArrayList<>(1);
		entryList.add(entry);

		try (Scheduler<UUID, Instant> scheduler = new LocalScheduler<>(configuration(entries, task))) {
			// Verify simple scheduling
			when(entries.peek()).thenReturn(entry, nullEntry);
			when(entries.iterator()).thenAnswer(invocation -> entryList.iterator());
			when(task.test(entry.getKey())).thenReturn(true);

			scheduler.schedule(entry.getKey(), entry.getValue());

			verify(entries).add(entry.getKey(), entry.getValue());

			Thread.sleep(500);

			// Verify that entry was removed from backing collection
			assertTrue(entryList.isEmpty());
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void failingTask() throws InterruptedException {
		ScheduledEntries<UUID, Instant> entries = mock(ScheduledEntries.class);
		Predicate<UUID> task = mock(Predicate.class);

		Map.Entry<UUID, Instant> entry = Map.entry(UUID.randomUUID(), Instant.now());
		Map.Entry<UUID, Instant> nullEntry = null;
		List<Map.Entry<UUID, Instant>> entryList = new ArrayList<>(1);
		entryList.add(entry);

		try (Scheduler<UUID, Instant> scheduler = new LocalScheduler<>(configuration(entries, task))) {
			// Verify that a failing scheduled task does not trigger removal
			when(entries.peek()).thenReturn(entry, nullEntry);
			when(entries.iterator()).thenAnswer(invocation -> entryList.iterator());
			when(task.test(entry.getKey())).thenReturn(false);

			scheduler.schedule(entry.getKey(), entry.getValue());

			verify(entries).add(entry.getKey(), entry.getValue());

			Thread.sleep(500);

			// Verify that entry was not removed from backing collection
			assertFalse(entryList.isEmpty());
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void retryUntilSuccessfulTask() throws InterruptedException {
		ScheduledEntries<UUID, Instant> entries = mock(ScheduledEntries.class);
		Predicate<UUID> task = mock(Predicate.class);

		Map.Entry<UUID, Instant> entry = Map.entry(UUID.randomUUID(), Instant.now());
		List<Map.Entry<UUID, Instant>> entryList = new ArrayList<>(1);
		entryList.add(entry);

		try (Scheduler<UUID, Instant> scheduler = new LocalScheduler<>(configuration(entries, task))) {
			// Verify that a failing scheduled task does not trigger removal
			when(entries.peek()).thenReturn(entry, entry, null);
			when(entries.iterator()).thenAnswer(invocation -> entryList.iterator());
			when(task.test(entry.getKey())).thenReturn(false, true);

			scheduler.schedule(entry.getKey(), entry.getValue());

			verify(entries).add(entry.getKey(), entry.getValue());

			Thread.sleep(500);

			// Verify that entry was eventually removed from backing collection
			assertTrue(entryList.isEmpty());
		}
	}

	@Test
	public void cancel() {
		ScheduledEntries<UUID, Instant> entries = mock(ScheduledEntries.class);
		Predicate<UUID> task = mock(Predicate.class);

		Map.Entry<UUID, Instant> entry = Map.entry(UUID.randomUUID(), Instant.now());

		try (Scheduler<UUID, Instant> scheduler = new LocalScheduler<>(configuration(entries, task))) {
			when(entries.peek()).thenReturn(entry);

			scheduler.cancel(entry.getKey());

			verify(entries).remove(entry.getKey());
		}
	}
}
