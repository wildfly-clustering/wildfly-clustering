/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.util.EnumSet;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.client.hotrod.RemoteCacheManagerAdmin;
import org.infinispan.client.hotrod.RemoteSchemasAdmin;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.infinispan.commons.configuration.BasicConfiguration;

/**
 * A {@link RemoteCacheManagerAdmin} decorator.
 * @author Paul Ferraro
 */
public class RemoteCacheManagerAdminDecorator implements RemoteCacheManagerAdmin {

	private final RemoteCacheContainer container;
	private final RemoteCacheManagerAdmin admin;

	RemoteCacheManagerAdminDecorator(RemoteCacheContainer container, RemoteCacheManagerAdmin admin) {
		this.container = container;
		this.admin = admin;
	}

	@Override
	public RemoteCacheManagerAdmin withFlags(AdminFlag... flags) {
		return new RemoteCacheManagerAdminDecorator(this.container, this.admin.withFlags(flags));
	}

	@Override
	public RemoteCacheManagerAdmin withFlags(EnumSet<AdminFlag> flags) {
		return new RemoteCacheManagerAdminDecorator(this.container, this.admin.withFlags(flags));
	}

	@Override
	public void createTemplate(String name, BasicConfiguration configuration) {
		this.admin.createTemplate(name, configuration);
	}

	@Override
	public void removeTemplate(String name) {
		this.admin.removeTemplate(name);
	}

	@Override
	public <K, V> RemoteCache<K, V> createCache(String name, String template) throws HotRodClientException {
		return new RemoteCacheDecorator<>(this.container, this.admin.createCache(name, template));
	}

	@Deprecated(forRemoval = true)
	@Override
	public <K, V> RemoteCache<K, V> createCache(String name, org.infinispan.client.hotrod.DefaultTemplate template) throws HotRodClientException {
		return new RemoteCacheDecorator<>(this.container, this.admin.createCache(name, template));
	}

	@Override
	public <K, V> RemoteCache<K, V> createCache(String name, BasicConfiguration configuration) throws HotRodClientException {
		return new RemoteCacheDecorator<>(this.container, this.admin.createCache(name, configuration));
	}

	@Override
	public <K, V> RemoteCache<K, V> getOrCreateCache(String name, String template) throws HotRodClientException {
		return new RemoteCacheDecorator<>(this.container, this.admin.getOrCreateCache(name, template));
	}

	@Deprecated(forRemoval = true)
	@Override
	public <K, V> RemoteCache<K, V> getOrCreateCache(String name, org.infinispan.client.hotrod.DefaultTemplate template) throws HotRodClientException {
		return new RemoteCacheDecorator<>(this.container, this.admin.getOrCreateCache(name, template));
	}

	@Override
	public <K, V> RemoteCache<K, V> getOrCreateCache(String name, BasicConfiguration configuration) throws HotRodClientException {
		return new RemoteCacheDecorator<>(this.container, this.admin.getOrCreateCache(name, configuration));
	}

	@Override
	public void removeCache(String name) throws HotRodClientException {
		this.admin.removeCache(name);
	}

	@Override
	public void reindexCache(String name) throws HotRodClientException {
		this.admin.reindexCache(name);
	}

	@Override
	public void updateIndexSchema(String cacheName) throws HotRodClientException {
		this.admin.updateIndexSchema(cacheName);
	}

	@Override
	public void updateConfigurationAttribute(String cacheName, String attribute, String value) throws HotRodClientException {
		this.admin.updateConfigurationAttribute(cacheName, attribute, value);
	}

	@Override
	public void assignAlias(String aliasName, String cacheName) throws HotRodClientException {
		this.admin.assignAlias(aliasName, cacheName);
	}

	@Override
	public RemoteSchemasAdmin schemas() {
		return this.admin.schemas();
	}
}
