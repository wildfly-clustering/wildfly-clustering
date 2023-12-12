/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import org.wildfly.clustering.server.dispatcher.Command;

/**
 * @author Paul Ferraro
 */
public class ContainsCommand<I, M> implements Command<Boolean, CacheEntryScheduler<I, M>, RuntimeException> {

	private final I id;

	ContainsCommand(I id) {
		this.id = id;
	}

	@Override
	public Boolean execute(CacheEntryScheduler<I, M> scheduler) {
		return scheduler.contains(this.id);
	}

	I getId() {
		return this.id;
	}

	@Override
	public String toString() {
		return String.format("%s[%s]", this.getClass().getSimpleName(), this.id);
	}
}
