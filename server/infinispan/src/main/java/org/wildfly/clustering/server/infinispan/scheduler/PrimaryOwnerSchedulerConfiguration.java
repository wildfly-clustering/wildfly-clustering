/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.time.Duration;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.infinispan.dispatcher.CacheContainerCommandDispatcherFactory;
import org.wildfly.clustering.server.util.Invoker;

/**
 * @author Paul Ferraro
 */
public interface PrimaryOwnerSchedulerConfiguration<I, M> {

	String getName();

	CacheContainerCommandDispatcherFactory getCommandDispatcherFactory();

	CacheEntryScheduler<I, M> getScheduler();

	Function<I, CacheContainerGroupMember> getAffinity();

	default BiFunction<I, M, ScheduleCommand<I, M>> getScheduleCommandFactory() {
		return ScheduleWithTransientMetaDataCommand::new;
	}

	default Invoker getInvoker() {
		return Invoker.retrying(List.of(Duration.ZERO, Duration.ofMillis(10), Duration.ofMillis(100)));
	}
}
