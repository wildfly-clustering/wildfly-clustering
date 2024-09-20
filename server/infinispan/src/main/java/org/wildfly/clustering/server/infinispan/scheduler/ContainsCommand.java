/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

/**
 * Command that determine if a given entry is known to the scheduler.
 * @param <I> the identifier type of scheduled entries
 * @param <M> the meta data type
 * @author Paul Ferraro
 */
public class ContainsCommand<I, M> extends AbstractPrimaryOwnerCommand<I, M, Boolean> {

	ContainsCommand(I id) {
		super(id);
	}

	@Override
	public Boolean execute(CacheEntryScheduler<I, M> scheduler) {
		return scheduler.contains(this.getId());
	}
}
