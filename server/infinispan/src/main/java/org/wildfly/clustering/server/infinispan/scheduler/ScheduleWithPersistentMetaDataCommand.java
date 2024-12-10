/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

/**
 * Command that schedules an item, where its meta data is persisted.
 * @param <I> the scheduled entry identifier type
 * @param <M> the scheduled entry metadata type
 * @author Paul Ferraro
 */
public class ScheduleWithPersistentMetaDataCommand<I, M> extends ScheduleCommand<I, M> {

	public ScheduleWithPersistentMetaDataCommand(I id, M metaData) {
		super(id, metaData);
	}
}
