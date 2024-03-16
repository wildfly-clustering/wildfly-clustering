/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.IsolationLevel;
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

	public EmbeddedCacheProperties(Configuration config) {
		this.transactional = config.transaction().transactionMode().isTransactional();
		this.lockOnWrite = this.transactional && (config.transaction().lockingMode() == LockingMode.PESSIMISTIC);
		this.lockOnRead = this.lockOnWrite && (config.locking().lockIsolationLevel() == IsolationLevel.REPEATABLE_READ);
		boolean clustered = config.clustering().cacheMode().needsStateTransfer();
		boolean hasStore = config.persistence().usingStores();
		this.marshalling = clustered || hasStore;
		this.persistent = clustered || (hasStore && !config.persistence().passivation()) || config.memory().isOffHeap();
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
}
