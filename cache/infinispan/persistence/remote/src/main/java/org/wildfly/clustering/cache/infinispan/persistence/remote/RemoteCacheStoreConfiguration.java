/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.persistence.remote;

import java.util.function.Consumer;

import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.infinispan.client.hotrod.configuration.RemoteCacheConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.client.hotrod.transaction.lookup.RemoteTransactionManagerLookup;
import org.infinispan.commons.configuration.BuiltBy;
import org.infinispan.commons.configuration.ConfigurationFor;
import org.infinispan.commons.configuration.attributes.AttributeDefinition;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.configuration.cache.AbstractStoreConfiguration;
import org.infinispan.configuration.cache.AsyncStoreConfiguration;

/**
 * The configuration of a HotRod store.
 * @author Paul Ferraro
 */
@BuiltBy(RemoteCacheStoreConfigurationBuilder.class)
@ConfigurationFor(RemoteCacheStore.class)
public class RemoteCacheStoreConfiguration extends AbstractStoreConfiguration<RemoteCacheStoreConfiguration> implements Consumer<RemoteCacheConfigurationBuilder> {
	enum Element {
		HOTROD_STORE
	}

	static final AttributeDefinition<RemoteCacheContainer> CONTAINER = AttributeDefinition.builder("container", null, RemoteCacheContainer.class).build();
	static final AttributeDefinition<String> TEMPLATE = AttributeDefinition.builder("template", null, String.class).build();
	static final AttributeDefinition<String> CONFIGURATION = AttributeDefinition.builder("configuration", """
{
	"distributed-cache": {
		"mode" : "SYNC",
		"transaction" : {
			"mode" : "NON_XA",
			"locking" : "PESSIMISTIC"
		}
	}
}""", String.class).build();

	/**
	 * Creates the configuration of a remote cache store.
	 * @param attributes the set of attributes
	 * @param async asynchronous store configuration
	 */
	public RemoteCacheStoreConfiguration(AttributeSet attributes, AsyncStoreConfiguration async) {
		super(Element.HOTROD_STORE, attributes, async);
	}

	/**
	 * Returns the remote cache container associated with this cache store.
	 * @return the remote cache container associated with this cache store.
	 */
	public RemoteCacheContainer container() {
		return this.attributes.attribute(CONTAINER).get();
	}

	/**
	 * Returns the remote cache configuration for use by this cache store.
	 * @return the remote cache configuration for use by this cache store.
	 */
	public String configuration() {
		return this.attributes.attribute(CONFIGURATION).get();
	}

	/**
	 * Returns the remote cache configuration template for use by this cache store.
	 * @return the remote cache configuration template for use by this cache store.
	 */
	public String template() {
		return this.attributes.attribute(TEMPLATE).get();
	}

	@Override
	public void accept(RemoteCacheConfigurationBuilder builder) {
		boolean transactional = this.attributes.attribute(TRANSACTIONAL).get();
		builder.forceReturnValues(false)
				.nearCacheMode(NearCacheMode.DISABLED)
				.transactionMode(transactional ? TransactionMode.NON_XA : TransactionMode.NONE)
				;
		if (transactional) {
			builder.transactionManagerLookup(RemoteTransactionManagerLookup.getInstance());
		}
		String template = this.attributes.attribute(TEMPLATE).get();
		if (template != null) {
			builder.templateName(template);
		} else {
			builder.configuration(this.attributes.attribute(CONFIGURATION).get());
		}
	}

	@Override
	public String toString() {
		return "HotRodStoreConfiguration{attributes=" + this.attributes + '}';
	}
}
