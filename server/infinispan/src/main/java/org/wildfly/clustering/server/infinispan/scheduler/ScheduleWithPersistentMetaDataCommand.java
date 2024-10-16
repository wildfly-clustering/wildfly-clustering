/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

/**
 * Command that schedules an item, where its meta data is persisted.
 * @param <I> the identifier type of the entry to schedule
 * @param <M> the meta data type of the entry to schedule
 * @author Paul Ferraro
 */
public class ScheduleWithPersistentMetaDataCommand<I, M> extends ScheduleCommand<I, M> {

	public ScheduleWithPersistentMetaDataCommand(I id, M metaData) {
		super(id, metaData);
	}
}
