/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.scheduler.SchedulerService;

/**
 * @author Paul Ferraro
 */
public class LocalSchedulerServiceTestCase {

	static LocalSchedulerService.Configuration<UUID> configuration(ScheduledEntries<UUID, Instant> entries, Predicate<UUID> task) {
		return new LocalSchedulerService.Configuration<>() {
			@Override
			public String getName() {
				return "test";
			}

			public ScheduledEntries<UUID, Instant> getScheduledEntries() {
				return entries;
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

	@Test
	public void lifecycle() {
		ScheduledEntries<UUID, Instant> entries = ScheduledEntries.queued();
		Predicate<UUID> task = mock(Predicate.class);

		Map.Entry<UUID, Instant> entry = Map.entry(UUID.randomUUID(), Instant.now());

		try (SchedulerService<UUID, Instant> scheduler = new LocalSchedulerService<>(configuration(entries, task))) {

			// Verify simple scheduling
			when(task.test(entry.getKey())).thenReturn(true);

			scheduler.schedule(entry.getKey(), entry.getValue());

			waitUntilEmpty(entries);

			// Tasks do not execute while scheduler is not started
			verifyNoInteractions(task);
			assertThat(entries).isNotEmpty();

			scheduler.start();

			waitUntilEmpty(entries);

			// Verify that entry was removed from backing collection
			verify(task).test(entry.getKey());
			assertThat(entries).isEmpty();
		}
	}

	@Test
	public void successfulTask() {
		ScheduledEntries<UUID, Instant> entries = ScheduledEntries.queued();
		Predicate<UUID> task = mock(Predicate.class);

		Map.Entry<UUID, Instant> entry = Map.entry(UUID.randomUUID(), Instant.now());

		try (SchedulerService<UUID, Instant> scheduler = new LocalSchedulerService<>(configuration(entries, task))) {
			scheduler.start();

			when(task.test(entry.getKey())).thenReturn(true);

			scheduler.schedule(entry.getKey(), entry.getValue());

			waitUntilEmpty(entries);

			// Verify that entry was removed from backing collection
			assertThat(entries).isEmpty();
			verify(task).test(entry.getKey());
		}
	}

	@Test
	public void failingTask() {
		ScheduledEntries<UUID, Instant> entries = ScheduledEntries.queued();
		Predicate<UUID> task = mock(Predicate.class);

		Map.Entry<UUID, Instant> entry = Map.entry(UUID.randomUUID(), Instant.now());

		try (SchedulerService<UUID, Instant> scheduler = new LocalSchedulerService<>(configuration(entries, task))) {
			scheduler.start();

			when(task.test(entry.getKey())).thenReturn(false);

			scheduler.schedule(entry.getKey(), entry.getValue());

			waitUntilEmpty(entries);

			// Verify that entry was not removed from backing collection
			assertThat(entries).isNotEmpty();
			verify(task, atLeastOnce()).test(entry.getKey());
		}
	}

	@Test
	public void retryUntilSuccessfulTask() {
		ScheduledEntries<UUID, Instant> entries = ScheduledEntries.queued();
		Predicate<UUID> task = mock(Predicate.class);

		Map.Entry<UUID, Instant> entry = Map.entry(UUID.randomUUID(), Instant.now());

		try (SchedulerService<UUID, Instant> scheduler = new LocalSchedulerService<>(configuration(entries, task))) {
			scheduler.start();

			// Verify that a failing scheduled task does not trigger removal
			when(task.test(entry.getKey())).thenReturn(false, true);

			scheduler.schedule(entry.getKey(), entry.getValue());

			waitUntilEmpty(entries);

			// Verify that entry was eventually removed from backing collection
			assertThat(entries).isEmpty();
			verify(task, times(2)).test(entry.getKey());
		}
	}

	@Test
	public void cancel() {
		ScheduledEntries<UUID, Instant> entries = mock(ScheduledEntries.class);
		Predicate<UUID> task = mock(Predicate.class);

		Map.Entry<UUID, Instant> entry = Map.entry(UUID.randomUUID(), Instant.now());

		try (SchedulerService<UUID, Instant> scheduler = new LocalSchedulerService<>(configuration(entries, task))) {
			when(entries.peek()).thenReturn(entry);

			scheduler.cancel(entry.getKey());

			verify(entries).remove(entry.getKey());
		}
	}

	private static void waitUntilEmpty(ScheduledEntries<UUID, Instant> entries) {
		// Wait until empty of timeout has elapsed.
		Instant stop = Instant.now().plus(Duration.ofSeconds(1));
		while ((entries.peek() != null) && Instant.now().isBefore(stop)) {
			Thread.yield();
		}
	}
}
