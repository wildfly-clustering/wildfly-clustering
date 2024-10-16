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
public class ScheduleWithTransientMetaDataCommand<I, M> extends ScheduleCommand<I, M> {

	public ScheduleWithTransientMetaDataCommand(I id, M metaData) {
		super(id, metaData);
	}

	@Override
	protected M getPersistentMetaData() {
		return null;
	}
}
