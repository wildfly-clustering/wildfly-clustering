/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.scheduler;

import org.wildfly.clustering.server.service.DecoratedService;

/**
 * A scheduler service decorator.
 * @param <K> the scheduled entry key type
 * @param <V> the scheduled entry value type
 * @author Paul Ferraro
 */
public class DecoratedSchedulerService<K, V> extends DecoratedService implements SchedulerService<K, V> {

	private final SchedulerService<K, V> scheduler;

	/**
	 * Creates a decorated scheduler service.
	 * @param scheduler the decorated scheduler service.
	 */
	public DecoratedSchedulerService(SchedulerService<K, V> scheduler) {
		super(scheduler);
		this.scheduler = scheduler;
	}

	@Override
	public void schedule(K key, V value) {
		this.scheduler.schedule(key, value);
	}

	@Override
	public void cancel(K key) {
		this.scheduler.cancel(key);
	}

	@Override
	public boolean contains(K key) {
		return this.scheduler.contains(key);
	}

	@Override
	public void close() {
		this.scheduler.close();
	}
}
