/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import org.infinispan.client.hotrod.RemoteCache;
import org.wildfly.clustering.cache.CacheProperties;

/**
 * @author Paul Ferraro
 */
public class RemoteCacheProperties implements CacheProperties {

	private final boolean transactional;

	public RemoteCacheProperties(RemoteCache<?, ?> cache) {
		this.transactional = cache.getTransactionManager() != null;
	}

	@Override
	public boolean isLockOnRead() {
		return false;
	}

	@Override
	public boolean isLockOnWrite() {
		return false;
	}

	@Override
	public boolean isMarshalling() {
		return true;
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	@Override
	public boolean isTransactional() {
		return this.transactional;
	}

	@Override
	public boolean isActive() {
		return true;
	}
}
