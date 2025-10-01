/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.container;

import java.time.Duration;
import java.util.function.Predicate;

import org.infinispan.commons.configuration.Builder;
import org.infinispan.commons.configuration.Combine;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.configuration.cache.ConfigurationBuilder;

/**
 * Builder of a {@link DataContainerConfiguration}.
 * @author Paul Ferraro
 */
public class DataContainerConfigurationBuilder implements Builder<DataContainerConfiguration> {

	private final AttributeSet attributes;

	/**
	 * Creates a new configuration builder.
	 * @param builder the builder of the associated cache configuration
	 */
	public DataContainerConfigurationBuilder(ConfigurationBuilder builder) {
		this();
	}

	DataContainerConfigurationBuilder() {
		this.attributes = new AttributeSet(DataContainerConfiguration.class, DataContainerConfiguration.EVICTABLE, DataContainerConfiguration.IDLE_TIMEOUT);
	}

	/**
	 * Specifies a predicate use to determine whether a given cache entry can be auto-evicted.
	 * @param <K> the cache key type
	 * @param evictable a predicate use to determine whether a given cache entry can be auto-evicted.
	 * @return a reference to this builder
	 */
	public <K> DataContainerConfigurationBuilder evictable(Predicate<K> evictable) {
		this.attributes.attribute(DataContainerConfiguration.EVICTABLE).set(evictable);
		return this;
	}

	/**
	 * Specifies the duration of time after which an evictable idle cache entry may be evicted.
	 * @param timeout an idle timeout
	 * @return a reference to this builder
	 */
	public DataContainerConfigurationBuilder idleTimeout(Duration timeout) {
		this.attributes.attribute(DataContainerConfiguration.IDLE_TIMEOUT).set(timeout);
		return this;
	}

	@Override
	public void validate() {
	}

	@Override
	public DataContainerConfiguration create() {
		return new DataContainerConfiguration(this.attributes);
	}

	@Override
	public DataContainerConfigurationBuilder read(DataContainerConfiguration template, Combine combine) {
		this.attributes.read(template.attributes(), combine);
		return this;
	}

	@Override
	public AttributeSet attributes() {
		return this.attributes;
	}
}
