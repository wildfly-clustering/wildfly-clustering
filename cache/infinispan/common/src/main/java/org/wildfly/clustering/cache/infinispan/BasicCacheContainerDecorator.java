/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan;

import java.util.Set;

import org.infinispan.commons.api.BasicCache;
import org.infinispan.commons.api.BasicCacheContainer;

/**
 * Decorator of a {@link BasicCacheContainer}.
 * @author Paul Ferraro
 */
public class BasicCacheContainerDecorator implements BasicCacheContainer {

	private final BasicCacheContainer container;

	/**
	 * Creates a new cache container decorator
	 * @param container the decorated cache container
	 */
	protected BasicCacheContainerDecorator(BasicCacheContainer container) {
		this.container = container;
	}

	@Override
	public void start() {
		this.container.start();
	}

	@Override
	public void stop() {
		this.container.stop();
	}

	@Override
	public <K, V> BasicCache<K, V> getCache() {
		return this.container.getCache();
	}

	@Override
	public <K, V> BasicCache<K, V> getCache(String cacheName) {
		return this.container.getCache(cacheName);
	}

	@Override
	public void stopCache(String cacheName) {
		this.container.stopCache(cacheName);
	}

	@Override
	public Set<String> getCacheNames() {
		return this.container.getCacheNames();
	}

	@Override
	public int hashCode() {
		return this.container.toString().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return (object != null) ? this.container.toString().equals(object.toString()) : false;
	}

	@Override
	public String toString() {
		return this.container.toString();
	}
}
