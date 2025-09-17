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

	@SuppressWarnings("rawtypes")
	static final AttributeDefinition<Predicate> EVICTABLE = AttributeDefinition.builder("evictable", org.wildfly.clustering.function.Predicate.always(), Predicate.class)
			.copier(IdentityAttributeCopier.identityCopier())
			.immutable()
			.build();

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

	public <K> Predicate<K> evictable() {
		return this.attributes.attribute(EVICTABLE).get();
	}

	public Duration idleTimeout() {
		return this.attributes.attribute(IDLE_TIMEOUT).get();
	}

	@Override
	public boolean matches(DataContainerConfiguration configuration) {
		return this.attributes.matches(configuration.attributes);
	}
}
