/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.function.BiFunction;
import java.util.function.Function;

import io.github.resilience4j.retry.RetryConfig;

import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.infinispan.dispatcher.CacheContainerCommandDispatcherFactory;

/**
 * Encapsulates configuration of a {@link PrimaryOwnerScheduler}.
 * @param <I> the scheduled entry identifier type
 * @param <M> the scheduled entry metadata type
 * @author Paul Ferraro
 */
public interface PrimaryOwnerSchedulerConfiguration<I, M> {

	String getName();

	CacheContainerCommandDispatcherFactory getCommandDispatcherFactory();

	Scheduler<I, M> getScheduler();

	Function<I, CacheContainerGroupMember> getAffinity();

	default BiFunction<I, M, ScheduleCommand<I, M>> getScheduleCommandFactory() {
		return ScheduleWithTransientMetaDataCommand::new;
	}

	RetryConfig getRetryConfig();
}
