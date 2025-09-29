/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.function.BiFunction;

import io.github.resilience4j.retry.RetryConfig;

import org.wildfly.clustering.server.infinispan.CacheContainerGroup;
import org.wildfly.clustering.server.infinispan.affinity.GroupMemberAffinityConfiguration;
import org.wildfly.clustering.server.infinispan.dispatcher.CacheContainerCommandDispatcherFactory;

/**
 * Encapsulates configuration of a {@link PrimaryOwnerScheduler}.
 * @param <I> the scheduled entry identifier type
 * @param <M> the scheduled entry metadata type
 * @author Paul Ferraro
 */
public interface PrimaryOwnerSchedulerConfiguration<I, M> extends GroupMemberAffinityConfiguration<I> {
	/**
	 * Returns the name of the primary owner scheduler.
	 * @return the name of the primary owner scheduler.
	 */
	String getName();

	/**
	 * Returns the command dispatcher factory for this scheduler.
	 * @return the command dispatcher factory for this scheduler.
	 */
	CacheContainerCommandDispatcherFactory getCommandDispatcherFactory();

	@Override
	default CacheContainerGroup getGroup() {
		return this.getCommandDispatcherFactory().getGroup();
	}

	/**
	 * Returns the delegated scheduler.
	 * @return the delegated scheduler.
	 */
	Scheduler<I, M> getScheduler();

	/**
	 * Returns the factory for creating a scheduler command.
	 * @return the factory for creating a scheduler command.
	 */
	default BiFunction<I, M, ScheduleCommand<I, M>> getScheduleCommandFactory() {
		return ScheduleWithTransientMetaDataCommand::new;
	}

	/**
	 * Returns the retry configuration.
	 * @return the retry configuration.
	 */
	RetryConfig getRetryConfig();
}
