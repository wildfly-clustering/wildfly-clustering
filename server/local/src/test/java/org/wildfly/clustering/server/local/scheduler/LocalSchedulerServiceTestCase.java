/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.wildfly.clustering.function.Predicate;
import org.wildfly.clustering.server.scheduler.SchedulerService;

/**
 * @author Paul Ferraro
 */
public class LocalSchedulerServiceTestCase {

	private final Predicate<UUID> task = mock(Predicate.class);
	private final ScheduledEntries<UUID, Instant> entries = ScheduledEntries.queued();
	private final LocalSchedulerService.Configuration<UUID> configuration = new LocalSchedulerService.Configuration<UUID>() {
		@Override
		public String getName() {
			return "test";
		}

		@Override
		public Predicate<UUID> getTask() {
			return LocalSchedulerServiceTestCase.this.task;
		}

		@Override
		public ScheduledEntries<UUID, Instant> getScheduledEntries() {
			return LocalSchedulerServiceTestCase.this.entries;
		}

		@Override
		public ThreadFactory getThreadFactory() {
			return Thread::new;
		}
	};

	@Test
	public void queue() throws InterruptedException {
		List<UUID> keys = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
		Duration interval = Duration.ofMillis(200);

		doReturn(true).when(this.task).test(any());
		InOrder order = inOrder(this.task);

		try (SchedulerService<UUID, Instant> scheduler = new LocalSchedulerService<>(this.configuration)) {
			scheduler.start();
			Instant now = Instant.now();
			for (int i = 0; i < keys.size(); ++i) {
				scheduler.schedule(keys.get(i), now.plus(interval.multipliedBy(i + 1)));
			}

			this.waitUntilEmpty(interval.multipliedBy(keys.size() + 1));

			// Verify all tasks were executed, in order
			for (UUID key : keys) {
				order.verify(this.task).test(key);
			}
			verifyNoMoreInteractions(this.task);

			assertThat(this.entries).isEmpty();
		}
	}

	@Test
	public void lifecycle() throws InterruptedException {
		UUID key = UUID.randomUUID();

		doReturn(true).when(this.task).test(key);

		try (SchedulerService<UUID, Instant> scheduler = new LocalSchedulerService<>(this.configuration)) {

			scheduler.schedule(key, Instant.now().plus(Duration.ofMillis(100)));

			this.waitUntilEmpty(Duration.ofMillis(400));

			// Tasks do not execute while scheduler is not started
			verifyNoInteractions(this.task);
			assertThat(this.entries).isNotEmpty();

			scheduler.start();

			this.waitUntilEmpty(Duration.ofMillis(400));

			// Verify that entry was removed from backing collection
			verify(this.task, only()).test(key);
			assertThat(this.entries).isEmpty();

			scheduler.schedule(key, Instant.now().plus(Duration.ofMillis(100)));

			// Stop scheduler before its task runs
			scheduler.stop();

			this.waitUntilEmpty(Duration.ofMillis(400));

			verifyNoMoreInteractions(this.task);
			assertThat(this.entries).isNotEmpty();

			scheduler.start();

			this.waitUntilEmpty(Duration.ofMillis(400));

			verify(this.task, times(2)).test(key);
			assertThat(this.entries).isEmpty();
		}
	}

	@Test
	public void successfulTask() throws InterruptedException {
		UUID key = UUID.randomUUID();

		doReturn(true).when(this.task).test(key);

		try (SchedulerService<UUID, Instant> scheduler = new LocalSchedulerService<>(this.configuration)) {
			scheduler.start();

			scheduler.schedule(key, Instant.now().plus(Duration.ofMillis(100)));

			verifyNoInteractions(this.task);

			this.waitUntilEmpty(Duration.ofMillis(400));

			verify(this.task, only()).test(key);
			// Verify that entry was removed from backing collection
			assertThat(this.entries).isEmpty();
		}
	}

	@Test
	public void failingTask() throws InterruptedException {
		UUID key = UUID.randomUUID();

		doReturn(false).when(this.task).test(key);

		try (SchedulerService<UUID, Instant> scheduler = new LocalSchedulerService<>(this.configuration)) {
			scheduler.start();

			scheduler.schedule(key, Instant.now().plus(Duration.ofMillis(100)));

			verifyNoInteractions(this.task);

			// Let it retry for a bit
			this.waitUntilEmpty(Duration.ofMillis(500));

			verify(this.task, atLeastOnce()).test(key);

			// Verify that entry was not removed from backing collection
			assertThat(this.entries).isNotEmpty();
		}
	}

	@Test
	public void retryUntilSuccessfulTask() throws InterruptedException {
		UUID key = UUID.randomUUID();

		doReturn(false, false, false, true).when(this.task).test(key);

		try (SchedulerService<UUID, Instant> scheduler = new LocalSchedulerService<>(this.configuration)) {
			scheduler.start();

			scheduler.schedule(key, Instant.now().plus(Duration.ofMillis(100)));

			verifyNoInteractions(this.task);

			this.waitUntilEmpty(Duration.ofMillis(500));

			// Verify that entry was eventually removed from backing collection
			verify(this.task, times(4)).test(key);

			assertThat(this.entries).isEmpty();
		}
	}

	@Test
	public void cancel() throws InterruptedException {
		UUID key = UUID.randomUUID();

		doReturn(true).when(this.task).test(key);

		try (SchedulerService<UUID, Instant> scheduler = new LocalSchedulerService<>(this.configuration)) {
			scheduler.start();

			scheduler.schedule(key, Instant.now().plus(Duration.ofMillis(100)));

			verifyNoInteractions(this.task);

			scheduler.cancel(key);

			assertThat(this.entries).isEmpty();

			// Wait until task should have been triggered
			Thread.sleep(400);

			// Cancelled task should not be triggered
			verifyNoInteractions(this.task);
		}
	}

	private void waitUntilEmpty(Duration duration) throws InterruptedException {
		// Wait until empty of timeout has elapsed.
		Instant stop = Instant.now().plus(duration);
		// Busy wait until entries are empty or
		while ((this.entries.peek() != null) && Instant.now().isBefore(stop)) {
			Thread.sleep(10);
		}
	}
}
