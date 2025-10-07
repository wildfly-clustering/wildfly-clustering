/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import org.wildfly.clustering.server.scheduler.Scheduler;

/**
 * Command that determine if a given entry is known to the scheduler.
 * @param <K> the scheduled entry key type
 * @param <V> the scheduled entry value type
 * @author Paul Ferraro
 */
public class ContainsCommand<K, V> extends AbstractPrimaryOwnerCommand<K, V, Boolean> {
	/**
	 * Creates a contains command for a scheduled entry with the specified identifier
	 * @param id a scheduler entry identifier
	 */
	ContainsCommand(K key) {
		super(key);
	}

	@Override
	public Boolean execute(Scheduler<K, V> scheduler) {
		return scheduler.contains(this.getKey());
	}
}
