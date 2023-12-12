/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.Collection;
import java.util.stream.Collectors;

import org.wildfly.clustering.server.dispatcher.Command;

/**
 * @author Paul Ferraro
 */
public class EntriesCommand<I, M> implements  Command<Collection<I>, CacheEntryScheduler<I, M>, RuntimeException> {

	@Override
	public Collection<I> execute(CacheEntryScheduler<I, M> scheduler) {
		return scheduler.stream().collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
