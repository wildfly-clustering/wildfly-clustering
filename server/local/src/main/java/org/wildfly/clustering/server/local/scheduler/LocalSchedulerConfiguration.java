/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadFactory;
import java.util.function.Predicate;

/**
 * Encapsulates configuration of a {@link LocalScheduler}.
 * @param <T> the scheduled entry identifier type
 * @author Paul Ferraro
 */
public interface LocalSchedulerConfiguration<T> {
	/**
	 * Returns the name of this scheduler.
	 * @return the name of this scheduler.
	 */
	String getName();

	/**
	 * Returns the scheduled entries collection used by this scheduler.
	 * @return the scheduled entries collection used by this scheduler.
	 */
	default ScheduledEntries<T, Instant> getScheduledEntries() {
		return ScheduledEntries.sorted();
	}

	/**
	 * Returns a scheduled task.
	 * @return a scheduled task.
	 */
	Predicate<T> getTask();

	/**
	 * Returns a thread factory for use by this scheduler.
	 * @return a thread factory for use by this scheduler.
	 */
	ThreadFactory getThreadFactory();

	/**
	 * Returns the duration of time to wait for scheduled tasks to complete on {@link LocalScheduler#close}.
	 * @return the duration of time to wait for scheduled tasks to complete on {@link LocalScheduler#close}.
	 */
	Duration getCloseTimeout();
}
