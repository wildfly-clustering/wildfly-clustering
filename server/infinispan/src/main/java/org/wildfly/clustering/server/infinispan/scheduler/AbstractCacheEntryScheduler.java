/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.server.scheduler.Scheduler;

/**
 * Abstract {@link CacheEntryScheduler} that delegates to a local scheduler.
 * @param <I> the scheduled object identifier type
 * @param <K> the cache entry key type
 * @param <V> the cache entry value type
 * @param <M> the scheduled object metadata type
 * @author Paul Ferraro
 */
public abstract class AbstractCacheEntryScheduler<I, K extends Key<I>, V, M> implements CacheEntryScheduler<I, K, V, M> {

	private final Scheduler<I, Instant> scheduler;
	private final Function<M, Optional<Instant>> instant;

	protected AbstractCacheEntryScheduler(Scheduler<I, Instant> scheduler, Function<M, Optional<Instant>> instant) {
		this.scheduler = scheduler;
		this.instant = instant;
	}

	@Override
	public void schedule(I id, M metaData) {
		Optional<Instant> instant = this.instant.apply(metaData);
		if (instant.isPresent()) {
			this.scheduler.schedule(id, instant.get());
		}
	}

	@Override
	public void cancel(I id) {
		this.scheduler.cancel(id);
	}

	@Override
	public boolean contains(I id) {
		return this.scheduler.contains(id);
	}

	@Override
	public void close() {
		this.scheduler.close();
	}

	@Override
	public String toString() {
		return this.scheduler.toString();
	}
}
