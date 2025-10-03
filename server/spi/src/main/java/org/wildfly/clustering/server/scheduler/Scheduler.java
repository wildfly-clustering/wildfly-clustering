/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.scheduler;

/**
 * A task scheduler.
 * @param <K> the scheduled entry identifier type
 * @param <V> the scheduled entry value type
 * @author Paul Ferraro
 */
public interface Scheduler<K, V> {
	/**
	 * Schedules a task for the object with the specified identifier, using the specified metaData
	 * @param id the scheduled entry identifier
	 * @param value the scheduled entry value
	 */
	void schedule(K id, V value);

	/**
	 * Cancels a previously scheduled task for the object with the specified identifier.
	 * @param id an object identifier
	 */
	void cancel(K id);

	/**
	 * Indicates whether the entry with the specified identifier is scheduled.
	 * @param id an object identifier
	 * @return true, if the specified entry is scheduled, false otherwise.
	 */
	boolean contains(K id);
}
