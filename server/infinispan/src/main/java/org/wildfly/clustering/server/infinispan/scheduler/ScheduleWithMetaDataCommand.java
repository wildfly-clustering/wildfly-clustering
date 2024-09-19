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
public class ScheduleWithMetaDataCommand<I, M> extends AbstractPrimaryOwnerCommand<I, M, Void> implements ScheduleCommand<I, M> {

	private final M metaData;

	public ScheduleWithMetaDataCommand(I id, M metaData) {
		super(id);
		this.metaData = metaData;
	}

	@Override
	public M getMetaData() {
		return this.metaData;
	}
}
