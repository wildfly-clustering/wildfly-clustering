/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

/**
 * Command that scheduled an entry.
 * @param <I> the scheduled entry identifier type
 * @param <M> the scheduled entry metadata type
 * @author Paul Ferraro
 */
public class ScheduleCommand<I, M> extends AbstractPrimaryOwnerCommand<I, M, Void> {

	private final M metaData;

	/**
	 * Creates a schedule command for a scheduled entry with the specified identifier
	 * @param id a scheduler entry identifier
	 */
	ScheduleCommand(I id) {
		this(id, null);
	}

	/**
	 * Creates a schedule command for a scheduled entry with the specified identifier with the specified metadata
	 * @param id a scheduler entry identifier
	 * @param metaData the schedule entry metadata
	 */
	ScheduleCommand(I id, M metaData) {
		super(id);
		this.metaData = metaData;
	}

	/**
	 * Returns the metadata associated with this metadata.
	 * @return the metadata associated with this metadata.
	 */
	protected M getMetaData() {
		return this.metaData;
	}

	@Override
	public Void execute(Scheduler<I, M> scheduler) {
		I id = this.getId();
		if (this.metaData != null) {
			scheduler.schedule(id, this.metaData);
		} else {
			scheduler.schedule(id);
		}
		return null;
	}
}
