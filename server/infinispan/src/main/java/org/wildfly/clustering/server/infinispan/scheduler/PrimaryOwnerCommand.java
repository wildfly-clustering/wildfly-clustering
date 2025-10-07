/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import org.wildfly.clustering.server.dispatcher.Command;
import org.wildfly.clustering.server.scheduler.Scheduler;

/**
 * A command intended to be sent to the primary owner of a given cache entry.
 * @author Paul Ferraro
 * @param <K> the scheduled entry key type
 * @param <V> the scheduled entry value type
 * @param <R> the command return type
 */
public interface PrimaryOwnerCommand<K, V, R> extends Command<R, Scheduler<K, V>, RuntimeException> {
	/**
	 * Returns the key of a scheduled item.
	 * @return the key of a scheduled item.
	 */
	K getKey();
}
