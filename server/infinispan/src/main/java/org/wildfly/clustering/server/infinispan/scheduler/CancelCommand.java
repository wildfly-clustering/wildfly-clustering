/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import org.wildfly.clustering.server.scheduler.Scheduler;

/**
 * Command that cancels a previously scheduled entry.
 * @param <K> the scheduled entry key type
 * @param <V> the scheduled entry value type
 * @author Paul Ferraro
 */
public class CancelCommand<K, V> extends AbstractPrimaryOwnerCommand<K, V, Void> {
	/**
	 * Creates a cancel command for a scheduled entry with the specified key
	 * @param key a scheduled entry key
	 */
	public CancelCommand(K key) {
		super(key);
	}

	@Override
	public Void execute(Scheduler<K, V> scheduler) {
		scheduler.cancel(this.getKey());
		return null;
	}
}
