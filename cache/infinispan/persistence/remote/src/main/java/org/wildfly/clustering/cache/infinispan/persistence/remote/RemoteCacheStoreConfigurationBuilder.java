/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.persistence.remote;

import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.commons.CacheConfigurationException;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.configuration.cache.AbstractStoreConfiguration;
import org.infinispan.configuration.cache.AbstractStoreConfigurationBuilder;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;

/**
 * Builds a HotRod store configuration.
 * @author Paul Ferraro
 */
public class RemoteCacheStoreConfigurationBuilder extends AbstractStoreConfigurationBuilder<RemoteCacheStoreConfiguration, RemoteCacheStoreConfigurationBuilder> {

	/**
	 * Creates a builder for a remote cache store configuration.
	 * @param builder the parent builder
	 */
	public RemoteCacheStoreConfigurationBuilder(PersistenceConfigurationBuilder builder) {
		super(builder, new AttributeSet(RemoteCacheStoreConfiguration.class, AbstractStoreConfiguration.attributeDefinitionSet(), RemoteCacheStoreConfiguration.CONFIGURATION, RemoteCacheStoreConfiguration.CONTAINER, RemoteCacheStoreConfiguration.TEMPLATE));
	}

	/**
	 * Defines the remote cache container for this remote cache store.
	 * @param container a remote cache container
	 * @return a reference to this builder
	 */
	public RemoteCacheStoreConfigurationBuilder container(RemoteCacheContainer container) {
		this.attributes.attribute(RemoteCacheStoreConfiguration.CONTAINER).set(container);
		return this;
	}

	/**
	 * Defines the remote cache configuration for this remote cache store.
	 * @param configuration a remote cache configuration
	 * @return a reference to this builder
	 */
	public RemoteCacheStoreConfigurationBuilder configuration(String configuration) {
		this.attributes.attribute(RemoteCacheStoreConfiguration.CONFIGURATION).set(configuration);
		return this;
	}

	/**
	 * Defines the remote cache configuration template for this remote cache store.
	 * @param template a remote cache configuration template
	 * @return a reference to this builder
	 */
	public RemoteCacheStoreConfigurationBuilder template(String template) {
		this.attributes.attribute(RemoteCacheStoreConfiguration.TEMPLATE).set(template);
		return this;
	}

	@Override
	public RemoteCacheStoreConfiguration create() {
		return new RemoteCacheStoreConfiguration(this.attributes.protect(), this.async.create());
	}

	@Override
	public void validate() {
		if (this.attributes.attribute(RemoteCacheStoreConfiguration.CONTAINER).get() == null) {
			throw new CacheConfigurationException(RemoteCacheStoreConfiguration.CONTAINER.name());
		}
		super.validate();
	}

	@Override
	public RemoteCacheStoreConfigurationBuilder self() {
		return this;
	}
}
