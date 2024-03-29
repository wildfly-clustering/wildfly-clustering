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
public class ScheduleWithMetaDataCommand<I, M> implements ScheduleCommand<I, M> {

	private final I id;
	private final M metaData;

	public ScheduleWithMetaDataCommand(I id, M metaData) {
		this.id = id;
		this.metaData = metaData;
	}

	@Override
	public I getId() {
		return this.id;
	}

	@Override
	public M getMetaData() {
		return this.metaData;
	}

	@Override
	public String toString() {
		return String.format("%s[%s]", this.getClass().getSimpleName(), this.id);
	}
}
