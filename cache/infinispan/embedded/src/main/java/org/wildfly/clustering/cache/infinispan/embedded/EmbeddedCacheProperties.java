/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.IsolationLevel;
import org.infinispan.configuration.cache.StoreConfiguration;
import org.infinispan.transaction.LockingMode;
import org.wildfly.clustering.cache.CacheProperties;

/**
 * Eagerly calculates the properties of a cache configuration.
 * @author Paul Ferraro
 */
public class EmbeddedCacheProperties implements CacheProperties {

	private final boolean lockOnRead;
	private final boolean lockOnWrite;
	private final boolean marshalling;
	private final boolean persistent;
	private final boolean transactional;
	private final boolean distributed;

	/**
	 * Creates an encapsulation of the properties of the specified cache.
	 * @param cache an embedded cache
	 */
	public EmbeddedCacheProperties(Cache<?, ?> cache) {
		this(cache.getCacheConfiguration());
	}

	/**
	 * Creates an encapsulation of the properties of the specified cache configuration.
	 * @param configuration an embedded cache configuration
	 */
	public EmbeddedCacheProperties(Configuration configuration) {
		this.transactional = configuration.transaction().transactionMode().isTransactional();
		this.lockOnWrite = this.transactional && (configuration.transaction().lockingMode() == LockingMode.PESSIMISTIC);
		this.lockOnRead = this.lockOnWrite && (configuration.locking().lockIsolationLevel() == IsolationLevel.REPEATABLE_READ);
		boolean distributed = configuration.clustering().cacheMode().needsStateTransfer();
		boolean hasStore = configuration.persistence().usingStores();
		this.marshalling = distributed || hasStore || configuration.memory().isOffHeap();
		this.distributed = distributed || (hasStore && configuration.persistence().stores().stream().anyMatch(StoreConfiguration::shared));
		this.persistent = distributed || (hasStore && !configuration.persistence().passivation()) || configuration.memory().isOffHeap();
	}

	@Override
	public boolean isPersistent() {
		return this.persistent;
	}

	@Override
	public boolean isTransactional() {
		return this.transactional;
	}

	@Override
	public boolean isLockOnRead() {
		return this.lockOnRead;
	}

	@Override
	public boolean isLockOnWrite() {
		return this.lockOnWrite;
	}

	@Override
	public boolean isMarshalling() {
		return this.marshalling;
	}

	@Override
	public boolean isDistributed() {
		return this.distributed;
	}
}
