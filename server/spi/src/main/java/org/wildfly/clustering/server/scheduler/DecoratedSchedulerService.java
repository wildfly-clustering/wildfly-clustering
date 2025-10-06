/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.scheduler;

import org.wildfly.clustering.server.service.DecoratedService;

/**
 * A scheduler service decorator.
 * @param <I> the scheduled entry identifier type
 * @param <V> the scheduled entry value type
 * @author Paul Ferraro
 */
public class DecoratedSchedulerService<I, V> extends DecoratedService implements SchedulerService<I, V> {

	private final SchedulerService<I, V> scheduler;

	/**
	 * Creates a decorated scheduler service.
	 * @param scheduler the decorated scheduler service.
	 */
	public DecoratedSchedulerService(SchedulerService<I, V> scheduler) {
		super(scheduler);
		this.scheduler = scheduler;
	}

	@Override
	public void schedule(I id, V value) {
		this.scheduler.schedule(id, value);
	}

	@Override
	public void cancel(I id) {
		this.scheduler.cancel(id);
	}

	@Override
	public boolean contains(I id) {
		return this.scheduler.contains(id);
	}

	@Override
	public void close() {
		this.scheduler.close();
	}
}
