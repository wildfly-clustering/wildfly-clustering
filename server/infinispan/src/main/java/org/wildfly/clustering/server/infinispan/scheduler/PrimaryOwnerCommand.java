/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import org.wildfly.clustering.server.dispatcher.Command;

/**
 * A command intended to be sent to the primary owner of a given cache entry.
 * @author Paul Ferraro
 * @param <I> the identifier type of the cache entry
 * @param <M> the meta data type of the cache entry
 * @param <R> the command return type
 */
public interface PrimaryOwnerCommand<I, M, R> extends Command<R, CacheEntryScheduler<I, M>, RuntimeException> {

	I getId();
}
