/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.infinispan.CacheGroupConfiguration;
import org.wildfly.clustering.server.infinispan.affinity.UnaryGroupMemberAffinity;
import org.wildfly.clustering.server.infinispan.dispatcher.CacheContainerCommandDispatcherFactoryConfiguration;

/**
 * Encapsulates configuration of a {@link PrimaryOwnerScheduler}.
 * @param <I> the scheduled entry identifier type
 * @param <M> the scheduled entry metadata type
 * @author Paul Ferraro
 */
public interface PrimaryOwnerSchedulerConfiguration<I, M> extends CacheContainerCommandDispatcherFactoryConfiguration, CacheGroupConfiguration {

	/**
	 * Returns the delegated scheduler.
	 * @return the delegated scheduler.
	 */
	Scheduler<I, M> getScheduler();

	/**
	 * Returns the function returning the group member for which a given identifier has affinity.
	 * @return the function returning the group member for which a given identifier has affinity.
	 */
	default Function<I, CacheContainerGroupMember> getAffinity() {
		return new UnaryGroupMemberAffinity<>(this);
	}

	/**
	 * Returns the factory for creating a scheduler command.
	 * @return the factory for creating a scheduler command.
	 */
	default BiFunction<I, M, ScheduleCommand<I, M>> getScheduleCommandFactory() {
		return ScheduleWithTransientMetaDataCommand::new;
	}
}
