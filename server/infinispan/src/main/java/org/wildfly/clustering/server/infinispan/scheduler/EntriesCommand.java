/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.Collection;
import java.util.List;

import org.wildfly.clustering.server.dispatcher.Command;

/**
 * Command returning the identifiers of all scheduler entries.
 * @param <I> the scheduled entry identifier type
 * @param <M> the scheduled entry metadata type
 * @author Paul Ferraro
 * @deprecated To be removed without replacement
 */
@Deprecated(forRemoval = true)
public class EntriesCommand<I, M> implements Command<Collection<I>, Scheduler<I, M>, RuntimeException> {

	@Override
	public Collection<I> execute(Scheduler<I, M> scheduler) {
		return List.of();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
