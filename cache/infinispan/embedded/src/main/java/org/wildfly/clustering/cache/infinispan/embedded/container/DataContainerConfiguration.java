/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.container;

import java.time.Duration;
import java.util.function.Predicate;

import org.infinispan.commons.configuration.BuiltBy;
import org.infinispan.commons.configuration.attributes.AttributeDefinition;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.commons.configuration.attributes.IdentityAttributeCopier;
import org.infinispan.commons.configuration.attributes.Matchable;

/**
 * Configuration of a Caffeine-based data container.
 * @author Paul Ferraro
 */
@BuiltBy(DataContainerConfigurationBuilder.class)
public class DataContainerConfiguration implements Matchable<DataContainerConfiguration> {
	/** Attribute defining a predicate used to determine whether a given cache entry is evictable. */
	@SuppressWarnings("rawtypes")
	static final AttributeDefinition<Predicate> EVICTABLE = AttributeDefinition.builder("evictable", org.wildfly.clustering.function.Predicate.of(true), Predicate.class)
			.copier(IdentityAttributeCopier.identityCopier())
			.immutable()
			.build();

	/** Attribute defining the period of time after which an idle entry should be evicted. */
	static final AttributeDefinition<Duration> IDLE_TIMEOUT = AttributeDefinition.builder("idle-timeout", null, Duration.class)
			.copier(IdentityAttributeCopier.identityCopier())
			.immutable()
			.build();

	private final AttributeSet attributes;

	DataContainerConfiguration(AttributeSet attributes) {
		this.attributes = attributes;
	}

	AttributeSet attributes() {
		return this.attributes;
	}

	/**
	 * Returns the predicate used to determine which data container entries can be evicted.
	 * @param <K> the cache key
	 * @return the predicate used to determine which data container entries can be evicted.
	 */
	public <K> Predicate<K> evictable() {
		return this.attributes.attribute(EVICTABLE).get();
	}

	/**
	 * Returns the duration after which evictable idle data container entries should be evicted.
	 * @return the duration after which evictable idle data container entries should be evicted.
	 */
	public Duration idleTimeout() {
		return this.attributes.attribute(IDLE_TIMEOUT).get();
	}

	@Override
	public boolean matches(DataContainerConfiguration configuration) {
		return this.attributes.matches(configuration.attributes);
	}
}
