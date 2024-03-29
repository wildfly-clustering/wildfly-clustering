/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

/**
 * Command that scheduled an item using transient metadata.
 * @param <I> the identifier type of the entry to schedule
 * @param <M> the meta data type of the entry to schedule
 * @author Paul Ferraro
 */
public class ScheduleWithTransientMetaDataCommand<I, M> implements ScheduleCommand<I, M> {

	private final I id;
	private final transient M metaData;

	public ScheduleWithTransientMetaDataCommand(I id, M metaData) {
		this.id = id;
		this.metaData = metaData;
	}

	ScheduleWithTransientMetaDataCommand(I id) {
		this(id, null);
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
