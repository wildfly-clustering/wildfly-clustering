/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.provider;

import org.wildfly.clustering.server.GroupMember;
import org.wildfly.clustering.server.Registrar;

/**
 * A distributed registry of service providers.
 * Allows a client to query the group members that can provide a given service.
 * @author Paul Ferraro
 * @param <T> the service type
 * @param <M> the member type
 */
public interface ServiceProviderRegistrar<T, M extends GroupMember> extends ServiceProviderRegistry<T, M>, Registrar<T> {

	/**
	 * Registers the local group member as a provider of the specified service.
	 *
	 * @param service a service to register
	 * @return a service provider registration to be closed when the local group member no longer provides the specified service.
	 */
	@Override
	ServiceProviderRegistration<T, M> register(T service);

	/**
	 * Registers the local group member as a provider of the specified service, using the specified listener.
	 *
	 * @param service a service to register
	 * @param listener a registry listener
	 * @return a service provider registration to be closed when the local group member no longer provides the specified service.
	 */
	ServiceProviderRegistration<T, M> register(T service, ServiceProviderListener<M> listener);
}
