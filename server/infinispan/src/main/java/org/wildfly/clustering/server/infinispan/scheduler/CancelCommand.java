/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

/**
 * Command that cancels a previously scheduled item.
 * @param <I> the identifier type of the entry to cancel
 * @param <M> the meta data type of the entry to cancel
 * @author Paul Ferraro
 */
public class CancelCommand<I, M> extends AbstractPrimaryOwnerCommand<I, M, Void> {

	public CancelCommand(I id) {
		super(id);
	}

	@Override
	public Void execute(CacheEntryScheduler<I, M> scheduler) {
		scheduler.cancel(this.getId());
		return null;
	}
}
