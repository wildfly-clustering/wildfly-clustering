/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session;

import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.server.deployment.DeploymentConfiguration;
import org.wildfly.clustering.server.eviction.EvictionConfiguration;
import org.wildfly.clustering.server.immutable.Immutability;

/**
 * Encapsulates the configuration of a session manager.
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public interface SessionManagerFactoryConfiguration<SC> extends DeploymentConfiguration, EvictionConfiguration {
	/**.
	 * Returns the marshaller used to marshal session attributes.
	 * @return the marshaller used to marshal session attributes.
	 */
	ByteBufferMarshaller getMarshaller();

	/**
	 * Returns the provider of a session context.
	 * @return the provider of a session context.
	 */
	Supplier<SC> getSessionContextFactory();

	/**
	 * Returns a predicate used to determine the immutability of a given session attribute.
	 * @return a predicate used to determine the immutability of a given session attribute.
	 */
	Immutability getImmutability();

	/**
	 * Returns the strategy to use for persisting session attributes.
	 * @return the strategy to use for persisting session attributes.
	 */
	SessionAttributePersistenceStrategy getAttributePersistenceStrategy();
}
