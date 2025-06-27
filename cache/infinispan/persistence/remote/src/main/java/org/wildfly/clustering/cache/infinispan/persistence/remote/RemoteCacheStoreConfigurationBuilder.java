/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.persistence.remote;

import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.configuration.cache.AbstractStoreConfiguration;
import org.infinispan.configuration.cache.AbstractStoreConfigurationBuilder;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;

/**
 * Builds a HotRod store configuration.
 * @author Paul Ferraro
 */
public class RemoteCacheStoreConfigurationBuilder extends AbstractStoreConfigurationBuilder<RemoteCacheStoreConfiguration, RemoteCacheStoreConfigurationBuilder> {

	public RemoteCacheStoreConfigurationBuilder(PersistenceConfigurationBuilder builder) {
		super(builder, new AttributeSet(RemoteCacheStoreConfiguration.class, AbstractStoreConfiguration.attributeDefinitionSet(), RemoteCacheStoreConfiguration.CONFIGURATION, RemoteCacheStoreConfiguration.CONTAINER, RemoteCacheStoreConfiguration.TEMPLATE));
	}

	public RemoteCacheStoreConfigurationBuilder container(RemoteCacheContainer container) {
		this.attributes.attribute(RemoteCacheStoreConfiguration.CONTAINER).set(container);
		return this;
	}

	public RemoteCacheStoreConfigurationBuilder configuration(String configuration) {
		this.attributes.attribute(RemoteCacheStoreConfiguration.CONFIGURATION).set(configuration);
		return this;
	}

	public RemoteCacheStoreConfigurationBuilder template(String template) {
		this.attributes.attribute(RemoteCacheStoreConfiguration.TEMPLATE).set(template);
		return this;
	}

	@Override
	public RemoteCacheStoreConfiguration create() {
		return new RemoteCacheStoreConfiguration(this.attributes.protect(), this.async.create());
	}

	@Override
	public RemoteCacheStoreConfigurationBuilder self() {
		return this;
	}
}
