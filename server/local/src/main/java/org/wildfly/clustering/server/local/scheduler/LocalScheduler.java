/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.wildfly.clustering.server.scheduler.Scheduler;
import org.wildfly.clustering.server.util.BlockingReference;

/**
 * Scheduler that uses a single scheduled task in concert with a {@link ScheduledEntries}.
 * @param <T> the scheduled entry identifier type
 * @author Paul Ferraro
 */
public class LocalScheduler<T> implements Scheduler<T, Instant>, Runnable {
	private static final System.Logger LOGGER = System.getLogger(LocalScheduler.class.getName());

	private final String name;
	private final ScheduledExecutorService executor;
	private final ScheduledEntries<T, Instant> entries;
	private final Predicate<T> task;
	private final Duration closeTimeout;
	private final Supplier<Map.Entry<Map.Entry<T, Instant>, Future<?>>> schedule;
	private final Supplier<Map.Entry<Map.Entry<T, Instant>, Future<?>>> scheduleIfAbsent;
	private final BlockingReference.Writer<Map.Entry<Map.Entry<T, Instant>, Future<?>>> cancel;
	private final BlockingReference.Writer<Map.Entry<Map.Entry<T, Instant>, Future<?>>> reschedule;

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
		Supplier<Map.Entry<Map.Entry<T, Instant>, Future<?>>> scheduleFirst = this::scheduleFirst;
		UnaryOperator<Map.Entry<Map.Entry<T, Instant>, Future<?>>> cancel = entry -> {
			entry.getValue().cancel(true);
			return null;
		};
		UnaryOperator<Map.Entry<Map.Entry<T, Instant>, Future<?>>> reschedule = entry -> {
			cancel.apply(entry);
			return scheduleFirst.get();
		};
		BlockingReference<Map.Entry<Map.Entry<T, Instant>, Future<?>>> futureEntry = BlockingReference.of(null);
		BlockingReference.Writer<Map.Entry<Map.Entry<T, Instant>, Future<?>>> futureEntryWriter = futureEntry.writer(scheduleFirst);
		this.schedule = futureEntryWriter;
		this.scheduleIfAbsent = futureEntryWriter.when(Objects::isNull);
		this.cancel = futureEntry.writer(cancel);
		this.reschedule = futureEntry.writer(reschedule);
	}

	@Override
	public void schedule(T id, Instant instant) {
		LOGGER.log(System.Logger.Level.TRACE, "Scheduling {1} on local {0} scheduler for {2}", this.name, id, instant);
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
		LOGGER.log(System.Logger.Level.TRACE, "Canceling {1} on local {0} scheduler", this.name, id);
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
		LOGGER.log(System.Logger.Level.DEBUG, "Shutting down local {0} scheduler", this.name);
		this.executor.shutdown();
		if (!this.closeTimeout.isNegative() && !this.closeTimeout.isZero()) {
			try {
				LOGGER.log(System.Logger.Level.DEBUG, "Waiting for local {0} scheduler tasks to complete", this.name);
				this.executor.awaitTermination(this.closeTimeout.toMillis(), TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		LOGGER.log(System.Logger.Level.DEBUG, "Local {0} scheduler shutdown complete", this.name);
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
			LOGGER.log(System.Logger.Level.TRACE, "Executing task for {1} on local {0} scheduler", this.name, key);
			// Remove only if task is successful
			if (this.task.test(key)) {
				entries.remove();
			}
		}
		// Schedule next task
		this.schedule.get();
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
			return Map.entry(entry, future);
		} catch (RejectedExecutionException e) {
			return null;
		}
	}

	private void scheduleIfAbsent() {
		this.scheduleIfAbsent.get();
	}

	private void rescheduleIfEarlier(Instant instant) {
		this.reschedule.when(entry -> (entry != null) && instant.isBefore(entry.getKey().getValue())).get();
	}

	private void cancelIfPresent(T id) {
		this.cancel.when(entry -> (entry != null) && entry.getKey().getKey().equals(id)).get();
	}

	@Override
	public String toString() {
		return this.entries.toString();
	}
}
