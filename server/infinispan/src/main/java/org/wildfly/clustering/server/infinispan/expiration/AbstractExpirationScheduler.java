/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.expiration;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

import org.wildfly.clustering.server.expiration.ExpirationMetaData;
import org.wildfly.clustering.server.infinispan.scheduler.AbstractCacheEntryScheduler;
import org.wildfly.clustering.server.scheduler.Scheduler;


/**
 * An {@link AbstractCacheEntryScheduler} implementation suitable for expiration.
 * @author Paul Ferraro
 * @param <I> the identifier type of the scheduled object
 */
public abstract class AbstractExpirationScheduler<I> extends AbstractCacheEntryScheduler<I, ExpirationMetaData> {

	private static final Function<ExpirationMetaData, Optional<Instant>> EXPIRATION = new Function<>() {
		@Override
		public Optional<Instant> apply(ExpirationMetaData metaData) {
			return !metaData.isImmortal() ? Optional.of(metaData.getLastAccessTime().plus(metaData.getTimeout())) : Optional.empty();
		}
	};

	public AbstractExpirationScheduler(Scheduler<I, Instant> scheduler) {
		super(scheduler, EXPIRATION);
	}
}
