/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

/**
 * Scheduler that does not require predetermined entry meta data.
 * @author Paul Ferraro
 * @param <K> the scheduled entry identifier type
 * @param <V> the scheduled entry metadata type
 */
public interface Scheduler<K, V> extends org.wildfly.clustering.server.scheduler.Scheduler<K, V> {
	/**
	 * Schedules the specified identifier whose metadata must be read elsewhere.
	 * @param id a scheduled object identifier.
	 */
	void schedule(K id);
}
