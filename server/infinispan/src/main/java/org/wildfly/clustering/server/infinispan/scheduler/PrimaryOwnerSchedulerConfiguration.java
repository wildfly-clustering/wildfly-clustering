/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.time.Duration;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.server.dispatcher.CommandDispatcherFactory;
import org.wildfly.clustering.server.group.GroupMember;
import org.wildfly.clustering.server.util.Invoker;

/**
 * @author Paul Ferraro
 */
public interface PrimaryOwnerSchedulerConfiguration<I, M, GM extends GroupMember<Address>> {

	String getName();

	CommandDispatcherFactory<GM> getCommandDispatcherFactory();

	CacheEntryScheduler<I, M> getScheduler();

	Function<I, GM> getAffinity();

	default BiFunction<I, M, ScheduleCommand<I, M>> getScheduleCommandFactory() {
		return ScheduleWithTransientMetaDataCommand::new;
	}

	default Invoker getInvoker() {
		return Invoker.retrying(List.of(Duration.ZERO, Duration.ofMillis(10), Duration.ofMillis(100)));
	}
}
