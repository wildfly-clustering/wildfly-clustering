/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

/**
 * Command that cancels a previously scheduled item.
 * @param <I> the scheduled entry identifier type
 * @param <M> the scheduled entry metadata type
 * @author Paul Ferraro
 */
public class CancelCommand<I, M> extends AbstractPrimaryOwnerCommand<I, M, Void> {

	public CancelCommand(I id) {
		super(id);
	}

	@Override
	public Void execute(Scheduler<I, M> scheduler) {
		scheduler.cancel(this.getId());
		return null;
	}
}
