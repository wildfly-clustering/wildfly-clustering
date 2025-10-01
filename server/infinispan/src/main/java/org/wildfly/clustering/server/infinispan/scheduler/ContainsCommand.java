/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

/**
 * Command that determine if a given entry is known to the scheduler.
 * @param <I> the scheduled entry identifier type
 * @param <M> the scheduled entry metadata type
 * @author Paul Ferraro
 */
public class ContainsCommand<I, M> extends AbstractPrimaryOwnerCommand<I, M, Boolean> {
	/**
	 * Creates a contains command for a scheduled entry with the specified identifier
	 * @param id a scheduler entry identifier
	 */
	ContainsCommand(I id) {
		super(id);
	}

	@Override
	public Boolean execute(Scheduler<I, M> scheduler) {
		return scheduler.contains(this.getId());
	}
}
