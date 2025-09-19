/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session;

import java.util.function.Supplier;

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

	ByteBufferMarshaller getMarshaller();

	Supplier<SC> getSessionContextFactory();

	Immutability getImmutability();

	SessionAttributePersistenceStrategy getAttributePersistenceStrategy();
}
