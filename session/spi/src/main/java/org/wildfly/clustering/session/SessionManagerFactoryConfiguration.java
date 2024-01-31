/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session;

import java.util.OptionalInt;
import java.util.function.Supplier;

import org.wildfly.clustering.cache.CacheConfiguration;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.server.deployment.DeploymentConfiguration;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.session.container.ContainerFacadeProvider;

/**
 * Encapsulates the configuration of a session manager.
 * @param <S> the container-specific session facade type
 * @param <DC> the deployment context type
 * @param <L> the container-specific activation listener type
 * @param <SC> the local context type
 * @author Paul Ferraro
 */
public interface SessionManagerFactoryConfiguration<S, DC, L, SC, B extends Batch> extends CacheConfiguration<B>, DeploymentConfiguration {

	OptionalInt getMaxActiveSessions();

	ByteBufferMarshaller getMarshaller();

	Supplier<SC> getSessionContextFactory();

	Immutability getImmutability();

	ContainerFacadeProvider<S, DC, L> getContainerFacadeProvider();

	SessionAttributePersistenceStrategy getAttributePersistenceStrategy();
}
