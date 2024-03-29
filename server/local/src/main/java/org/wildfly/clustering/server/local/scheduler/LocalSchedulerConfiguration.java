/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadFactory;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Encapsulates configuration of a {@link LocalScheduler}.
 * @param <T> the scheduled entry identifier type
 * @author Paul Ferraro
 */
public interface LocalSchedulerConfiguration<T> {
	String getName();

	Supplier<ScheduledEntries<T, Instant>> getScheduledEntriesFactory();

	Predicate<T> getTask();

	ThreadFactory getThreadFactory();

	Duration getCloseTimeout();
}
