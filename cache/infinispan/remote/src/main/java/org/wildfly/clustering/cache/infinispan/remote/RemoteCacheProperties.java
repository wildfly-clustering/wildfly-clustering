/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.util.Optional;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.wildfly.clustering.cache.CacheProperties;

/**
 * @author Paul Ferraro
 */
public class RemoteCacheProperties implements CacheProperties {

	private final boolean transactional;
	private final boolean cached;

	public RemoteCacheProperties(RemoteCache<?, ?> cache) {
		// TODO query server configuration to determine whether reads are actually repeatable.
		this.transactional = cache.isTransactional();
		org.infinispan.client.hotrod.configuration.RemoteCacheConfiguration config = cache.getRemoteCacheContainer().getConfiguration().remoteCaches().get(cache.getName());
		this.cached = Optional.ofNullable(config).map(org.infinispan.client.hotrod.configuration.RemoteCacheConfiguration::nearCacheMode).orElse(NearCacheMode.DISABLED).enabled();
	}

	@Override
	public boolean isLockOnRead() {
		// Assume REPEATABLE_READ semantics, unless thwarted by near caching
		return this.transactional && !this.cached;
	}

	@Override
	public boolean isLockOnWrite() {
		// Assume PESSIMISTIC locking
		return this.transactional;
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
