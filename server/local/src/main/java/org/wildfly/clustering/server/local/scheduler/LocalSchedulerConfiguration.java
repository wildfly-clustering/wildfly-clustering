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

	default ScheduledEntries<T, Instant> getScheduledEntries() {
		return ScheduledEntries.sorted();
	}

	Predicate<T> getTask();

	ThreadFactory getThreadFactory();

	Duration getCloseTimeout();
}
