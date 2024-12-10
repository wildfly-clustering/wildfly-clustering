/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import org.wildfly.clustering.server.dispatcher.Command;

/**
 * A command intended to be sent to the primary owner of a given cache entry.
 * @author Paul Ferraro
 * @param <I> the scheduled object identifier type
 * @param <M> the scheduled object metadata type
 * @param <R> the command return type
 */
public interface PrimaryOwnerCommand<I, M, R> extends Command<R, Scheduler<I, M>, RuntimeException> {

	I getId();
}
