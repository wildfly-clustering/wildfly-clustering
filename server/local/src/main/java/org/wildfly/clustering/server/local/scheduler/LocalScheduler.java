/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.jboss.logging.Logger;
import org.wildfly.clustering.server.scheduler.Scheduler;

/**
 * Scheduler that uses a single scheduled task in concert with a {@link ScheduledEntries}.
 * @param <T> the scheduled entry identifier type
 * @author Paul Ferraro
 */
public class LocalScheduler<T> implements Scheduler<T, Instant>, Runnable {
	private static final Logger LOGGER = Logger.getLogger(LocalScheduler.class);

	private final String name;
	private final ScheduledExecutorService executor;
	private final ScheduledEntries<T, Instant> entries;
	private final Predicate<T> task;
	private final Duration closeTimeout;

	private volatile Map.Entry<Map.Entry<T, Instant>, Future<?>> futureEntry = null;

	public LocalScheduler(LocalSchedulerConfiguration<T> configuration) {
		this.name = configuration.getName();
		this.entries = configuration.getScheduledEntries();
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, configuration.getThreadFactory());
		executor.setKeepAliveTime(1L, TimeUnit.MINUTES);
		executor.allowCoreThreadTimeOut(true);
		executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		executor.setRemoveOnCancelPolicy(this.entries.isSorted());
		this.executor = executor;
		this.task = configuration.getTask();
		this.closeTimeout = configuration.getCloseTimeout();
	}

	@Override
	public void schedule(T id, Instant instant) {
		LOGGER.tracef("Scheduling %s on local %s scheduler for %s", id, this.name, instant);
		this.entries.add(id, instant);
		// Reschedule next task if this item is now the earliest task
		// This can only be the case if the ScheduledEntries is sorted
		if (this.entries.isSorted()) {
			this.rescheduleIfEarlier(instant);
		}
		// Schedule the next task if nothing is currently scheduled.
		this.scheduleIfAbsent();
	}

	@Override
	public void cancel(T id) {
		LOGGER.tracef("Canceling %s on local %s scheduler", id, this.name);
		// Cancel scheduled task if this item was currently scheduled
		if (this.entries.isSorted()) {
			this.cancelIfPresent(id);
		}
		this.entries.remove(id);
		// Ensure a new item is scheduled
		if (this.entries.isSorted()) {
			this.scheduleIfAbsent();
		}
	}

	@Override
	public boolean contains(T id) {
		return this.entries.contains(id);
	}

	@Override
	public void close() {
		LOGGER.debugf("Shutting down local %s scheduler", this.name);
		this.executor.shutdown();
		if (!this.closeTimeout.isNegative() && !this.closeTimeout.isZero()) {
			try {
				LOGGER.debugf("Waiting for local %s scheduler tasks to complete", this.name);
				this.executor.awaitTermination(this.closeTimeout.toMillis(), TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		LOGGER.debugf("Local %s scheduler shutdown complete", this.name);
	}

	@Override
	public void run() {
		// Iterate over ScheduledEntries until we encounter a future entry
		Iterator<Map.Entry<T, Instant>> entries = this.entries.iterator();
		while (entries.hasNext()) {
			if (Thread.currentThread().isInterrupted() || this.executor.isShutdown()) return;
			Map.Entry<T, Instant> entry = entries.next();
			// If this is a future entry, break out of loop
			if (entry.getValue().isAfter(Instant.now())) break;
			T key = entry.getKey();
			LOGGER.tracef("Executing task for %s on local %s scheduler", key, this.name);
			// Remove only if task is successful
			if (this.task.test(key)) {
				entries.remove();
			}
		}
		// Schedule next task
		synchronized (this) {
			this.futureEntry = this.scheduleFirst();
		}
	}

	private Map.Entry<Map.Entry<T, Instant>, Future<?>> scheduleFirst() {
		Map.Entry<T, Instant> entry = this.entries.peek();
		return (entry != null) ? this.schedule(entry) : null;
	}

	private Map.Entry<Map.Entry<T, Instant>, Future<?>> schedule(Map.Entry<T, Instant> entry) {
		Duration delay = Duration.between(Instant.now(), entry.getValue());
		long millis = !delay.isNegative() ? delay.toMillis() + 1 : 0;
		try {
			Future<?> future = this.executor.schedule(this, millis, TimeUnit.MILLISECONDS);
			return new SimpleImmutableEntry<>(entry, future);
		} catch (RejectedExecutionException e) {
			return null;
		}
	}

	private void scheduleIfAbsent() {
		if (this.futureEntry == null) {
			synchronized (this) {
				if (this.futureEntry == null) {
					this.futureEntry = this.scheduleFirst();
				}
			}
		}
	}

	private void rescheduleIfEarlier(Instant instant) {
		if (this.futureEntry != null) {
			synchronized (this) {
				if (this.futureEntry != null) {
					if (instant.isBefore(this.futureEntry.getKey().getValue())) {
						this.futureEntry.getValue().cancel(true);
						this.futureEntry = this.scheduleFirst();
					}
				}
			}
		}
	}

	private void cancelIfPresent(T id) {
		if (this.futureEntry != null) {
			synchronized (this) {
				if (this.futureEntry != null) {
					if (this.futureEntry.getKey().getKey().equals(id)) {
						this.futureEntry.getValue().cancel(true);
						this.futureEntry = null;
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return this.entries.toString();
	}
}
