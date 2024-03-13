/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import org.wildfly.clustering.server.dispatcher.Command;

/**
 * Command that cancels a previously scheduled item.
 * @param <I> the identifier type of the entry to cancel
 * @param <M> the meta data type of the entry to cancel
 * @author Paul Ferraro
 */
public class CancelCommand<I, M> implements Command<Void, CacheEntryScheduler<I, M>, RuntimeException> {

	private final I id;

	public CancelCommand(I id) {
		this.id = id;
	}

	I getId() {
		return this.id;
	}

	@Override
	public Void execute(CacheEntryScheduler<I, M> scheduler) {
		scheduler.cancel(this.id);
		return null;
	}

	@Override
	public String toString() {
		return String.format("%s[%s]", this.getClass().getSimpleName(), this.id);
	}
}
