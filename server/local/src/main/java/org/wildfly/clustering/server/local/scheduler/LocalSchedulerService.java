/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.scheduler;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.function.UnaryOperator;
import org.wildfly.clustering.server.scheduler.SchedulerService;
import org.wildfly.clustering.server.service.SimpleService;
import org.wildfly.clustering.server.util.BlockingReference;

/**
 * Scheduler that uses a single scheduled task in concert with a {@link ScheduledEntries}.
 * @param <K> the scheduled entry key type
 * @author Paul Ferraro
 */
public class LocalSchedulerService<K> extends SimpleService implements SchedulerService<K, Instant>, Runnable {
	private static final System.Logger LOGGER = System.getLogger(LocalSchedulerService.class.getName());

	/**
	 * Encapsulates configuration of a {@link LocalSchedulerService}.
	 * @param <K> the scheduled entry key type
	 */
	public interface Configuration<K> {
		/**
		 * Returns the name of this scheduler.
		 * @return the name of this scheduler.
		 */
		String getName();

		/**
		 * Returns the scheduled entries collection used by this scheduler.
		 * @return the scheduled entries collection used by this scheduler.
		 */
		default ScheduledEntries<K, Instant> getScheduledEntries() {
			return ScheduledEntries.sorted();
		}

		/**
		 * Returns the scheduled task.
		 * @return the scheduled task.
		 */
		Predicate<K> getTask();

		/**
		 * Returns a thread factory for use by this scheduler.
		 * @return a thread factory for use by this scheduler.
		 */
		ThreadFactory getThreadFactory();

		/**
		 * Returns the duration of time to wait for scheduled tasks to complete on {@link SchedulerService#close}.
		 * @return the duration of time to wait for scheduled tasks to complete on {@link SchedulerService#close}.
		 */
		default Duration getCloseTimeout() {
			return Duration.ZERO;
		}
	}

	private final String name;
	private final ScheduledExecutorService executor;
	private final ScheduledEntries<K, Instant> entries;
	private final Predicate<K> task;
	private final Duration closeTimeout;
	private final Supplier<Map.Entry<Map.Entry<K, Instant>, Future<?>>> schedule;
	private final Supplier<Map.Entry<Map.Entry<K, Instant>, Future<?>>> scheduleIfAbsent;
	private final BlockingReference.Writer<Map.Entry<Map.Entry<K, Instant>, Future<?>>> cancel;
	private final BlockingReference.Writer<Map.Entry<Map.Entry<K, Instant>, Future<?>>> reschedule;

	/**
	 * Creates a local scheduler using the specified configuration.
	 * @param configuration the scheduler configuration
	 */
	public LocalSchedulerService(Configuration<K> configuration) {
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
		Supplier<Map.Entry<Map.Entry<K, Instant>, Future<?>>> scheduleFirst = this::scheduleFirst;
		Consumer<Future<?>> cancelFuture = future -> future.cancel(true);
		Consumer<Map.Entry<Map.Entry<K, Instant>, Future<?>>> cancelEntry = cancelFuture.<Map.Entry<Map.Entry<K, Instant>, Future<?>>>compose(Map.Entry::getValue).when(Objects::nonNull);
		UnaryOperator<Map.Entry<Map.Entry<K, Instant>, Future<?>>> cancel = UnaryOperator.of(cancelEntry, Supplier.of(null));
		UnaryOperator<Map.Entry<Map.Entry<K, Instant>, Future<?>>> reschedule = UnaryOperator.of(cancelEntry, scheduleFirst);
		BlockingReference<Map.Entry<Map.Entry<K, Instant>, Future<?>>> futureEntry = BlockingReference.of(null);
		BlockingReference.Writer<Map.Entry<Map.Entry<K, Instant>, Future<?>>> futureEntryWriter = futureEntry.writer(scheduleFirst);
		this.schedule = futureEntryWriter;
		this.scheduleIfAbsent = futureEntryWriter.when(Objects::isNull);
		this.cancel = futureEntry.writer(cancel);
		this.reschedule = futureEntry.writer(reschedule);
	}

	@Override
	public void schedule(K key, Instant instant) {
		LOGGER.log(System.Logger.Level.TRACE, "Scheduling {1} on local {0} scheduler for {2}", this.name, key, instant);
		this.entries.add(key, instant);
		// Reschedule next task if this item is now the earliest task
		// This can only be the case if the ScheduledEntries is sorted
		if (this.entries.isSorted()) {
			this.rescheduleIfEarlier(instant);
		}
		// Schedule the next task if nothing is currently scheduled.
		this.scheduleIfAbsent();
	}

	@Override
	public void cancel(K key) {
		LOGGER.log(System.Logger.Level.TRACE, "Canceling {1} on local {0} scheduler", this.name, key);
		// Cancel scheduled task if this item was currently scheduled
		if (this.entries.isSorted()) {
			this.cancelIfPresent(key);
		}
		this.entries.remove(key);
		// Ensure a new item is scheduled
		if (this.entries.isSorted()) {
			this.scheduleIfAbsent();
		}
	}

	@Override
	public boolean contains(K key) {
		return this.entries.contains(key);
	}

	@Override
	public void start() {
		super.start();
		this.schedule.get();
	}

	@Override
	public void stop() {
		this.cancel.get();
		super.stop();
	}

	@SuppressWarnings("removal")
	@Override
	public void close() {
		LOGGER.log(System.Logger.Level.DEBUG, "Shutting down local {0} scheduler", this.name);
		PrivilegedAction<Void> action = new PrivilegedAction<>() {
			@Override
			public Void run() {
				LocalSchedulerService.this.executor.shutdown();
				return null;
			}
		};
		AccessController.doPrivileged(action);
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
		Iterator<Map.Entry<K, Instant>> entries = this.entries.iterator();
		while (entries.hasNext()) {
			if (Thread.currentThread().isInterrupted() || this.executor.isShutdown()) return;
			Map.Entry<K, Instant> entry = entries.next();
			// If this is a future entry, break out of loop
			if (entry.getValue().isAfter(Instant.now())) break;
			K key = entry.getKey();
			LOGGER.log(System.Logger.Level.DEBUG, "Executing task for {1} on local {0} scheduler", this.name, key);
			// Remove only if task is successful
			if (this.task.test(key)) {
				entries.remove();
			}
		}
		// Schedule next task
		this.schedule.get();
	}

	private Map.Entry<Map.Entry<K, Instant>, Future<?>> scheduleFirst() {
		Map.Entry<K, Instant> entry = this.entries.peek();
		return (entry != null) ? this.scheduleEntry(entry) : null;
	}

	private Map.Entry<Map.Entry<K, Instant>, Future<?>> scheduleEntry(Map.Entry<K, Instant> entry) {
		if (!this.isStarted()) return null;
		Instant now = Instant.now();
		Instant target = entry.getValue();
		Duration delay = now.isBefore(target) ? Duration.between(now, target) : Duration.ZERO;
		try {
			Future<?> future = this.executor.schedule(this, delay.toNanos(), TimeUnit.NANOSECONDS);
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

	private void cancelIfPresent(K id) {
		this.cancel.when(entry -> (entry != null) && entry.getKey().getKey().equals(id)).get();
	}

	@Override
	public String toString() {
		return this.entries.toString();
	}
}
