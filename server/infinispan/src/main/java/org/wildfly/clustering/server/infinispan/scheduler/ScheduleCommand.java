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
public interface ScheduleCommand<I, M> extends PrimaryOwnerCommand<I, M, Void> {

	/**
	 * Returns the meta data of the element to be scheduled.
	 * @return the meta data of the element to be scheduled.
	 */
	M getMetaData();

	@Override
	default Void execute(CacheEntryScheduler<I, M> scheduler) {
		I id = this.getId();
		M metaData = this.getMetaData();
		if (metaData != null) {
			scheduler.schedule(id, metaData);
		} else {
			scheduler.schedule(id);
		}
		return null;
	}
}
