/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan;

import java.time.Duration;

import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.function.Supplier;

/**
 * Abstract mutator providing max-idle customization.
 * @author Paul Ferraro
 */
public abstract class AbstractCacheEntryMutator implements CacheEntryMutator, java.util.function.Supplier<Duration> {

	private volatile java.util.function.Supplier<Duration> maxIdle = Supplier.of(Duration.ZERO);

	/**
	 * Creates a immortal cache entry mutator.
	 */
	protected AbstractCacheEntryMutator() {
	}

	@Override
	public CacheEntryMutator withMaxIdle(java.util.function.Supplier<Duration> maxIdle) {
		this.maxIdle = maxIdle;
		return this;
	}

	@Override
	public Duration get() {
		return this.maxIdle.get();
	}
}
