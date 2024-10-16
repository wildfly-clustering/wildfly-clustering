/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

/**
 * Command that scheduled an entry.
 * @param <I> the identifier type of the entry to schedule
 * @param <M> the meta data type of the entry to schedule
 * @author Paul Ferraro
 */
public class ScheduleCommand<I, M> extends AbstractPrimaryOwnerCommand<I, M, Void> {

	private final M metaData;

	ScheduleCommand(I id, M metaData) {
		super(id);
		this.metaData = metaData;
	}

	protected M getPersistentMetaData() {
		return this.metaData;
	}

	@Override
	public Void execute(CacheEntryScheduler<I, M> scheduler) {
		I id = this.getId();
		if (this.metaData != null) {
			scheduler.schedule(id, this.metaData);
		} else {
			scheduler.schedule(id);
		}
		return null;
	}
}
