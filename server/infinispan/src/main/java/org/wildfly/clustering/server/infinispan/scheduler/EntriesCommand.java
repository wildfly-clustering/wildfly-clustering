/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.Collection;

import org.wildfly.clustering.server.dispatcher.Command;

/**
 * Command returning the identifiers of all scheduler entries.
 * @param <I> the identifier type of scheduled entries
 * @param <M> the meta data type
 * @author Paul Ferraro
 */
public class EntriesCommand<I, M> implements Command<Collection<I>, CacheEntryScheduler<I, M>, RuntimeException> {

	@Override
	public Collection<I> execute(CacheEntryScheduler<I, M> scheduler) {
		return scheduler.stream().toList();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
