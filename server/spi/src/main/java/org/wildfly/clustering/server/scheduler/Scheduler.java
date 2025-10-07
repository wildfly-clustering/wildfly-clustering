/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.scheduler;

import java.util.Map;

/**
 * A task scheduler.
 * @param <K> the scheduled entry key type
 * @param <V> the scheduled entry value type
 * @author Paul Ferraro
 */
public interface Scheduler<K, V> {
	/**
	 * Schedules a task for the specified entry
	 * @param entry the scheduled entry
	 */
	default void schedule(Map.Entry<K, V> entry) {
		this.schedule(entry.getKey(), entry.getValue());
	}

	/**
	 * Schedules a task for the specified entry.
	 * @param key the scheduled entry key
	 * @param value the scheduled entry value
	 */
	void schedule(K key, V value);

	/**
	 * Cancels a previously scheduled task for the entry with the specified key.
	 * @param key the scheduled entry key
	 */
	void cancel(K key);

	/**
	 * Indicates whether the entry with the specified key is scheduled.
	 * @param key the scheduled entry key
	 * @return true, if the specified entry is scheduled, false otherwise.
	 */
	boolean contains(K key);
}
