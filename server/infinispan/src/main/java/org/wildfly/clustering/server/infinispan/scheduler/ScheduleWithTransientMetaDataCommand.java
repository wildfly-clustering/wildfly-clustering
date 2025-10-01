/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

/**
 * Command that scheduled an item using transient metadata.
 * @param <I> the scheduled entry identifier type
 * @param <M> the scheduled entry metadata type
 * @author Paul Ferraro
 */
public class ScheduleWithTransientMetaDataCommand<I, M> extends ScheduleCommand<I, M> {
	/**
	 * Creates a schedule command for a cache entry.
	 * @param id the identifier of a cache entry
	 * @param metaData the cache entry metadata
	 */
	public ScheduleWithTransientMetaDataCommand(I id, M metaData) {
		super(id, metaData);
	}

	@Override
	protected M getMetaData() {
		return null;
	}
}
