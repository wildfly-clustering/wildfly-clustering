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

	ScheduleCommand(I id) {
		this(id, null);
	}

	ScheduleCommand(I id, M metaData) {
		super(id);
		this.metaData = metaData;
	}

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
