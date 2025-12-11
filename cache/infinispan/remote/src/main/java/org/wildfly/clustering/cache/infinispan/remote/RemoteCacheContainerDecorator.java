/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.client.hotrod.RemoteCacheManagerAdmin;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.impl.protocol.HotRodConstants;
import org.infinispan.commons.marshall.Marshaller;
import org.wildfly.clustering.cache.infinispan.BasicCacheContainerDecorator;

/**
 * A decorator of a {@link RemoteCacheContainer}.
 * @author Paul Ferraro
 */
public class RemoteCacheContainerDecorator extends BasicCacheContainerDecorator implements RemoteCacheContainer {
	private final RemoteCacheContainer container;

	/**
	 * Creates a new remote cache container decorator
	 * @param container the decorated remote cache container
	 */
	protected RemoteCacheContainerDecorator(RemoteCacheContainer container) {
		super(container);
		this.container = container;
	}

	@Override
	public <K, V> RemoteCache<K, V> getCache() {
		return this.container.getCache(HotRodConstants.DEFAULT_CACHE_NAME);
	}

	@Override
	public <K, V> RemoteCache<K, V> getCache(String cacheName) {
		return this.container.getCache(cacheName);
	}

	@Override
	public Configuration getConfiguration() {
		return this.container.getConfiguration();
	}

	@Override
	public boolean isStarted() {
		return this.container.isStarted();
	}

	@Override
	public boolean switchToCluster(String clusterName) {
		return this.container.switchToCluster(clusterName);
	}

	@Override
	public boolean switchToDefaultCluster() {
		return this.container.switchToDefaultCluster();
	}

	@Override
	public String getCurrentClusterName() {
		return this.container.getCurrentClusterName();
	}

	@Override
	public Marshaller getMarshaller() {
		return this.container.getMarshaller();
	}

	@Override
	public boolean isTransactional(String cacheName) {
		return this.container.isTransactional(cacheName);
	}

	@Override
	public RemoteCacheManagerAdmin administration() {
		return this.container.administration();
	}
}
