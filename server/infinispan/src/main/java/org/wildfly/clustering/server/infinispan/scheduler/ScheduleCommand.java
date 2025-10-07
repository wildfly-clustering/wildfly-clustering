/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.util.Map;

import org.wildfly.clustering.server.scheduler.Scheduler;

/**
 * Command that scheduled an entry.
 * @param <K> the scheduled entry key type
 * @param <V> the scheduled entry value type
 * @author Paul Ferraro
 */
public class ScheduleCommand<K, V> extends AbstractPrimaryOwnerCommand<K, V, Void> {

	private final V value;

	/**
	 * Creates a schedule command for the specified entry.
	 * @param entry an entry to be scheduled
	 */
	public ScheduleCommand(Map.Entry<K, V> entry) {
		this(entry.getKey(), entry.getValue());
	}

	/**
	 * Creates a schedule command for the specified key and value.
	 * @param key the scheduled entry key
	 * @param value the scheduled entry value
	 */
	ScheduleCommand(K key, V value) {
		super(key);
		this.value = value;
	}

	/**
	 * Returns the value to be scheduled.
	 * @return the value to be scheduled.
	 */
	protected V getValue() {
		return this.value;
	}

	@Override
	Map.Entry<K, V> getParameter() {
		return Map.entry(this.getKey(), this.getValue());
	}

	@Override
	public Void execute(Scheduler<K, V> scheduler) {
		scheduler.schedule(this.getKey(), this.value);
		return null;
	}
}
